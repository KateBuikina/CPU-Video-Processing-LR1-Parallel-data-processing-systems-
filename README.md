# CPU-Based Multithreaded Video Processing 
*(Master’s Practical Assignment №1, 2025)*

## Overview (English)

This project implements **Variant 2** of the CPU multithreading assignment:  
*For each video frame, compute pixel intensity as the average of its R, G, and B components. Identify all pixels with intensity < 64 (i.e., in the first quarter of the 0–255 grayscale range), and draw a red outline (BGR: [0, 0, 255]) around each such pixel — specifically, on its 4-connected neighbors (up, down, left, right). Process all frames in parallel using Java’s `ExecutorService`, and benchmark performance across varying thread counts.*

The program:
- Loads a video file via GUI (JFileChooser)
- Reads all frames into memory as `Mat` objects
- Processes each frame independently in a configurable thread pool (2, 4, 6, 8, 10 threads)
- Writes the resulting video to disk (AVI, MJPG codec)
- Performs **5 timing runs per thread count**, logging execution time (in seconds)
- Fully compliant with the specification: grayscale conversion via averaging, intensity thresholding at 64, and red-pixel outlining of qualifying regions

Designed for academic evaluation of CPU parallelization efficiency in image-processing pipelines.

## Technical Details

- **Language**: Java 8+
- **Library**: OpenCV 4.x (core + video I/O)
- **Parallelism**: `java.util.concurrent.Executors.newFixedThreadPool`
- **Memory Model**: Frame buffering (all frames loaded before processing)
- **Output**: Same FPS and resolution as input; `_output.avi` suffix

> Note: The outlining logic uses Manhattan-neighborhood (4-directional) red coloring for pixels adjacent to any low-intensity pixel, not 8-connected. This follows the implementation in `processFrame()`.

---

## Обзор (русский)

Данный проект реализует **Вариант 2** практического задания по многопоточной обработке на CPU:  
*Для каждого кадра видео вычисляется интенсивность пикселя как среднее арифметическое значений R, G и B компонент. Пиксели с интенсивностью < 64 (т.е. попадающие в первый квант из четырёх возможных) обводятся красной рамкой — а именно, их 4-связные соседи (вверх, вниз, влево, вправо) окрашиваются в красный цвет (BGR: [0, 0, 255]). Обработка кадров выполняется параллельно с использованием пула потоков Java, а производительность оценивается при разном числе потоков.*

Программа:
- Загружает видео через графический диалог выбора файла (JFileChooser)
- Считывает все кадры в память в виде списка объектов `Mat`
- Обрабатывает каждый кадр независимо в пуле потоков (2, 4, 6, 8, 10 потоков — фиксированный набор)
- Записывает результат в видеофайл (контейнер AVI, кодек MJPG)
- Выполняет **по 5 запусков на каждое число потоков**, выводя время обработки в секундах
- Полностью соответствует ТЗ: усреднённая матрица интенсивности, порог 64, обводка красным

Цель — экспериментальная оценка масштабируемости CPU-обработки изображений при увеличении числа потоков.

## Технические особенности

- **Язык**: Java 8+
- **Библиотека**: OpenCV 4.x (модули `core`, `videoio`)
- **Многопоточность**: `Executors.newFixedThreadPool`
- **Модель памяти**: предварительная загрузка всех кадров (frame buffering)
- **Выходные данные**: видео с исходными FPS и разрешением; имя файла — исходное + `_output.avi`

> Примечание: в методе `processFrame()` реализована обводка **только по 4-связности** (без диагоналей), что соответствует формулировке «обвести пиксели границей» в варианте 2 и подтверждается условием `if (Math.abs(dy) == 1 || Math.abs(dx) == 1)`.
