import pandas as pd
import matplotlib.pyplot as plt

# CHANGE THE FILE NAME HERE
file_name = "part2-2"

# Read the CSV file
file_path = f'{file_name}.csv'
df = pd.read_csv(file_path, sep=';')

# Get the column name
column_name = "time_avg"
if column_name not in df.columns:
    print(f"Column {column_name} not found in the DataFrame")
    exit(1)

# Generate a box plot for the average execution time
plt.figure(figsize=(6, 4))  # Reduced plot size
box_plot = plt.boxplot(df[column_name], patch_artist=True)

# Change color and linewidth of the whiskers
for whisker in box_plot['whiskers']:
    whisker.set(color='#6495ed', linewidth=1.5)

# Change color and linewidth of the caps
for cap in box_plot['caps']:
    cap.set(color='#6495ed', linewidth=1.5)

# Change the color and line width of the boxes
for patch in box_plot['boxes']:
    patch.set(facecolor='#add8e6', edgecolor='#6495ed', linewidth=1.5)

plt.title(f'Matrix Line Multiplication - Parallelism Version 2', fontsize=16)  # Increased font size
plt.ylabel('Average Time (s)', fontsize=14)  # Increased font size
plt.xticks([1], ['c++'], fontsize=14)  # Increased font size
plt.grid(True)
plt.tight_layout()

# Increase the size of the y tick labels
plt.tick_params(axis='y', which='major', labelsize=12)

# Save the plot as a PDF file
plot_img_name = f'images/{file_name}_box_plot.pdf'
plt.savefig(plot_img_name, format='pdf')

plt.show()