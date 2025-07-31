## 🚫 NoMoreChanges for Eclipse

Tired of writing 'bug fixes' or 'changes' for your commits? **NoMoreChanges** is an intelligent Eclipse plugin designed to end that habit. It seamlessly integrates into your workflow by automatically generating a clear and descriptive commit message for you when you click the **Commit** button in the Git Staging view.

It analyzes your staged code to understand its purpose, helping you maintain a clean, understandable project history with zero extra clicks.

---
## ⚙️ Features

* **Automatic Generation on Commit**: No extra buttons to push. Simply click the standard **Commit** button in the Git Staging view, and a descriptive message is instantly generated and populated for you.
* **Context-Aware Analysis**: Intelligently analyzes code diffs (added, modified, and deleted lines) to understand the *intent* behind your changes.
* **Manual Refresh**: If you stage or unstage files, simply use the new **Refresh** button in the Staging view to ensure the AI has the latest context before you commit.
* **Configurable API Key**: Easily add your OpenAI API key through a dedicated menu in Eclipse's preferences.

---
## 🔑 Requirements & Configuration

Before you can use the plugin, you must configure it with your OpenAI API key.

1.  Obtain an **API key** from your [OpenAI account](https://platform.openai.com/api-keys).
2.  In Eclipse, navigate to `Window` > `Preferences`.
3.  Find and select the **NoMoreChanges** section in the preferences menu.
4.  Paste your API key into the designated field and save.

---
## 🛠️ Installation (via Dropins)

As this is a custom plugin, installation is done by placing the plugin file directly into your Eclipse installation directory.

1.  Download the latest `NoMoreChanges_x.x.x.jar` file from the [Releases page](https://github.com/ckmanees/no-more-changes/releases).
2.  Locate your Eclipse installation folder.
3.  Place the downloaded `.jar` file inside the `eclipse/dropins/` subdirectory.
4.  **Restart Eclipse** completely for the plugin to be loaded.

---
## 🤝 Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you'd like to help improve **NoMoreChanges**, please feel free to fork the repository, make your changes, and submit a pull request. You can also open an issue to report bugs or suggest new features.
