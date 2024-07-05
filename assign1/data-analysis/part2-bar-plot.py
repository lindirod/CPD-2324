import pandas as pd
import matplotlib.pyplot as plt

# Increase the size of all text in the plot
plt.rcParams.update({'font.size': 14})

# Define the names of the three CSV files and corresponding labels
file_names = ["part2-0", "part2-1", "part2-2"]
labels = ["No Parallelism", "Parallelism 1", "Parallelism 2"]

# Read each CSV file into a pandas DataFrame
dfs = [pd.read_csv(f'{file_name}.csv', sep=';') for file_name in file_names]

# Merge the three DataFrames on the 'n' column
df = pd.concat(dfs, keys=labels, names=['Implementation', 'Row'])

# Pivot the DataFrame to get 'n' as index and each file as a column
df_pivot = df.pivot_table(index='n', columns='Implementation', values='time_avg')

# Define the colors for the bars
colors = ["#FF8C00", "#008B8B", "#9932CC"] 

# Generate a bar plot for the average execution time from each DataFrame
ax = df_pivot.plot(kind='bar', figsize=(12, 8), width=0.8, color=colors)

plt.xlabel('Matrix Size (n)')
plt.ylabel('Average Execution Time (s)')
plt.title('Matrix Line Multiplication')

plt.xticks(rotation=45)
plt.grid(True)
plt.tight_layout()

# Save the plot as a PDF file
plot_img_name = 'images/part2_bar_plot.pdf'
plt.savefig(plot_img_name, format='pdf')

plt.show()