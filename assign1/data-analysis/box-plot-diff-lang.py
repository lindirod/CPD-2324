import pandas as pd
import matplotlib.pyplot as plt

# CHANGE THE FILE NAME HERE
file_name = "part1-2-1"

# Read the CSV file
file_path = f'{file_name}.csv'
df = pd.read_csv(file_path, sep=';')

# Get unique languages
languages = df['lang'].unique()

# Get the column name
column_name = "time_avg"
if column_name not in df.columns:
    print(f"Column {column_name} not found in the DataFrame")
    exit(1)

# Generate box plots for each language
plt.figure(figsize=(6, 4))  # Reduced plot size
colors = ['#add8e6', '#90ee90']  # Soft pastel colors
edge_colors = ['#6495ed', '#228b22']  # Darker shades for the edges
for i, lang in enumerate(languages):
    lang_data = df[df['lang'] == lang][column_name]
    box_plot = plt.boxplot(lang_data, positions=[i + 1], widths=0.5, patch_artist=True, labels=[lang])
    
    # Change color and linewidth of the whiskers
    for whisker in box_plot['whiskers']:
        whisker.set(color=edge_colors[i], linewidth=1.5)
    
    # Change color and linewidth of the caps
    for cap in box_plot['caps']:
        cap.set(color=edge_colors[i], linewidth=1.5)
    
    # Change the color and line width of the boxes
    for patch in box_plot['boxes']:
        patch.set(facecolor=colors[i], edgecolor=edge_colors[i], linewidth=1.5)

plt.title(f'Linear Matrix Multiplication', fontsize=16)  # Increased font size
plt.xlabel('Language', fontsize=14)  # Increased font size
plt.ylabel('Average Time (s)', fontsize=14)  # Increased font size
plt.grid(True)
plt.tight_layout()

# Increase the size of the tick labels
plt.tick_params(axis='both', which='major', labelsize=12)

# Save the plot as a PDF file
plot_img_name = f'images/{file_name}_box_plot.pdf'
plt.savefig(plot_img_name, format='pdf')

plt.show()