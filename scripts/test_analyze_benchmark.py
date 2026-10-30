"""Synthetic fixtures validate math/compatibility, never establish gameplay performance."""
import tempfile
import unittest
import zipfile
from pathlib import Path
from analyze_benchmark import analyze

class AnalysisTest(unittest.TestCase):
    def test_old_capture_and_gpu_correlation(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'sample.zip'
            with zipfile.ZipFile(path, 'w') as archive:
                archive.writestr('frames.csv', 'elapsed_ns,frame_ns,tick_inclusive_ns\n10000000,10000000,1000\n30000000,20000000,2000\n')
            result = analyze(path)
            self.assertAlmostEqual(result['metrics']['average_fps'], 1000 / 15)
            self.assertEqual(result['metrics']['one_percent_low_fps'], 50)
            self.assertIsNone(result['slowest_frames'][0]['gpu_interval_ms'])
            with zipfile.ZipFile(path, 'a') as archive:
                archive.writestr('gpu.csv', 'frame_index,game_renderer_gpu_interval_ns\n1,7000000\n')
            self.assertEqual(analyze(path)['slowest_frames'][0]['gpu_interval_ms'], 7)
    def test_empty_capture(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'empty.zip'
            with zipfile.ZipFile(path, 'w') as archive:
                archive.writestr('frames.csv', 'frame_ns\n0\n')
            with self.assertRaisesRegex(ValueError, 'positive frame'):
                analyze(path)

if __name__ == '__main__':
    unittest.main()
