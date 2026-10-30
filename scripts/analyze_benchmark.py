#!/usr/bin/env python3
"""Compare Blossom benchmark archives. No third-party dependencies or game launch required."""
import argparse
import csv
import io
import json
import math
import statistics
import zipfile
from pathlib import Path


def rows(archive, name):
    if name not in archive.namelist():
        return []
    with archive.open(name) as stream:
        return list(csv.DictReader(io.TextIOWrapper(stream, encoding='utf-8')))


def percentile(ordered, fraction):
    return ordered[max(0, math.ceil(len(ordered) * fraction) - 1)]


def analyze(path):
    with zipfile.ZipFile(path) as archive:
        frames = rows(archive, 'frames.csv')
        resources = rows(archive, 'resources.csv')
        gpu = {int(row['frame_index']): int(row['game_renderer_gpu_interval_ns'])
               for row in rows(archive, 'gpu.csv')}
        sections = rows(archive, 'sections.csv')
    samples = [(i, row, int(row['frame_ns'])) for i, row in enumerate(frames)
               if int(row['frame_ns']) > 0]
    if not samples:
        raise ValueError('Archive has no positive frame intervals')
    times = sorted(nanos / 1e6 for _, _, nanos in samples)
    metrics = {'frames': len(times), 'average_fps': 1000 / statistics.mean(times),
               'one_percent_low_fps': 1000 / statistics.mean(times[-max(1, math.ceil(len(times) * .01)):]),
               'point_one_percent_low_fps': 1000 / statistics.mean(times[-max(1, math.ceil(len(times) * .001)):]),
               'median_ms': percentile(times, .5), 'p95_ms': percentile(times, .95),
               'p99_ms': percentile(times, .99), 'p999_ms': percentile(times, .999),
               'worst_ms': times[-1], 'frames_over_50ms': sum(t > 50 for t in times)}
    for column in ('heap_used_bytes', 'direct_buffer_bytes', 'pending_chunk_jobs', 'pending_uploads', 'gc_ms_delta'):
        values = [int(row[column]) for row in resources if int(row[column]) >= 0]
        metrics['peak_' + column] = max(values) if values else None
    spikes = []
    for i, row, nanos in sorted(samples, key=lambda sample: sample[2], reverse=True)[:20]:
        index = int(row.get('frame_index', i))
        phases = sorted(((key.removesuffix('_inclusive_ns'), int(value) / 1e6)
                         for key, value in row.items() if key.endswith('_inclusive_ns')),
                        key=lambda pair: pair[1], reverse=True)
        spikes.append({'frame_index': index, 'frame_ms': nanos / 1e6,
                       'gpu_interval_ms': gpu[index] / 1e6 if index in gpu else None,
                       'inclusive_phases_ms': dict(phases)})
    ranked = sorted(sections, key=lambda row: int(row['self_ns']), reverse=True)[:20]
    return {'capture': str(path), 'metrics': metrics, 'slowest_frames': spikes,
            'largest_self_time_sections': ranked,
            'limitations': 'Inclusive phases overlap. GPU intervals include idle/dependencies. '
                           'Correlation is not causal attribution. Compare repeated identical workloads; '
                           'GC counters are not pause measurements. Missing GPU samples remain null.'}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('capture', type=Path)
    parser.add_argument('--baseline', type=Path)
    parser.add_argument('--json', type=Path, help='Write a machine-readable report')
    args = parser.parse_args()
    report = analyze(args.capture)
    if args.baseline:
        baseline = analyze(args.baseline)
        report['baseline'] = baseline['metrics']
        report['change_percent'] = {
            key: (value / baseline['metrics'][key] - 1) * 100
            for key, value in report['metrics'].items()
            if value is not None and baseline['metrics'].get(key) not in (None, 0)
        }
    for key, value in report['metrics'].items():
        print(f'{key}: {value:.3f}' if isinstance(value, float) else f'{key}: {value}')
    if 'change_percent' in report:
        print('Change vs baseline (%; higher FPS is better, lower frame times are better):')
        for key in ('average_fps', 'one_percent_low_fps', 'point_one_percent_low_fps', 'p99_ms', 'p999_ms'):
            print(f"  {key}: {report['change_percent'][key]:+.2f}%")
    print(report['limitations'])
    if args.json:
        args.json.write_text(json.dumps(report, indent=2) + '\n', encoding='utf-8')


if __name__ == '__main__':
    main()
