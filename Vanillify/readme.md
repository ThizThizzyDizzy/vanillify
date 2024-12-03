# Update Instructions
    (For updating to new MC versions)

- Check [AnvilGUI](https://github.com/WesJD/AnvilGUI) to verify if R-version or compatibility has changed
- Create dummy AnvilGUI & Vanillify Wrapper files for new R-version (in Vanillify project)
- Copy latest "VS" Subproject & rename to match new R-version
- Download & Extract dependencies from new CraftBukkit jar file, apply those to VS subproject
- Change CB package names to match new version
- Update AnvilGUI and Vanillify VersionMatcher classes to reflect changes made in [AnvilGUI's VersionMatcher.java](https://github.com/WesJD/AnvilGUI/blob/master/api/src/main/java/net/wesjd/anvilgui/version/VersionMatcher.java)
- Propogate any new changes from [VersionWrapper](https://github.com/WesJD/AnvilGUI/blob/master/abstraction/src/main/java/net/wesjd/anvilgui/version/VersionWrapper.java). If there are any changes:
    - Update VersionWrapper in ALL sub-projects
    - Update ALL individual wrapper classes in each sub-project (and the placeholders in the main project)
    - Don't forget to merge the 1_17_1_R1 special class into a subclass of 1_17_1 (same for 1_19_1_R1)
        - Maybe check that the modified 1_19_1_R1 thing actually functions? It probably doesn't.