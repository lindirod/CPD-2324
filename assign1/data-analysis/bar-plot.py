import pandas as pd
import matplotlib.pyplot as plt

# TODO: CHANGE THE FILE NAME HERE
file_name = "part2-0"

# Read the CSV file
file_path = f'{file_name}.csv'
df = pd.read_csv(file_path, sep=';')

# Generate a bar plot for the average execution time
# Two Languages
# ax = df.pivot(index='n', columns='lang', values='time_avg').plot(kind='bar', color=['#66c2a5', '#fc8d62'])
# One Language
ax = df.plot(kind='bar', x='n', y='time_avg', color='#1f77b4')

plt.xlabel('Matrix Size (n)')
plt.ylabel('Average Execution Time (s)')

# TODO: CHANGE THE PLOT TITLE HERE
plt.title('Matrix Line Multiplication Bar Plot - No Parallelism')

plt.legend([])
plt.xticks(rotation=45)
plt.grid(True)
plt.tight_layout()

# Save the plot as a PDF file
plot_img_name = f'images/{file_name}_bar_plot.pdf'
plt.savefig(plot_img_name, format='pdf')

plt.show()