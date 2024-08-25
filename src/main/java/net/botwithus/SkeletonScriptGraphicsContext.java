package net.botwithus;

import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.imgui.ImGui;
import net.botwithus.rs3.script.ScriptConsole;
import net.botwithus.rs3.script.ScriptGraphicsContext;

public class SkeletonScriptGraphicsContext extends ScriptGraphicsContext {
    private final Constructor script;
    private long scriptStartTime;
    private int startingXP;
    boolean isScriptRunning = false;
    private final int startingConstructionLevel;

    public SkeletonScriptGraphicsContext(ScriptConsole console, Constructor script) {
        super(console);
        this.script = script;
        this.startingXP = Skills.CONSTRUCTION.getSkill().getExperience();
        this.scriptStartTime = System.currentTimeMillis();
        this.startingConstructionLevel = script.getStartingConstructionLevel();
    }

    public void drawSettings() {
        ImGui.PushStyleColor(21, RGBToFloat(47), RGBToFloat(79), RGBToFloat(79), 1.0F);
        ImGui.PushStyleColor(18, RGBToFloat(255), RGBToFloat(255), RGBToFloat(255), 1.0F);
        ImGui.PushStyleColor(5, RGBToFloat(47), RGBToFloat(79), RGBToFloat(79), 1.0F);
        ImGui.PushStyleColor(2, RGBToFloat(0), RGBToFloat(0), RGBToFloat(0), 0.9F);
        ImGui.PushStyleColor(7, RGBToFloat(47), RGBToFloat(79), RGBToFloat(79), 1.0F);
        ImGui.PushStyleColor(11, RGBToFloat(47), RGBToFloat(79), RGBToFloat(79), 1.0F);
        ImGui.PushStyleColor(22, RGBToFloat(64), RGBToFloat(67), RGBToFloat(67), 1.0F);
        ImGui.PushStyleColor(27, RGBToFloat(47), RGBToFloat(79), RGBToFloat(79), 1.0F);
        ImGui.PushStyleColor(30, RGBToFloat(47), RGBToFloat(79), RGBToFloat(79), 1.0F);
        ImGui.PushStyleColor(0, RGBToFloat(173), RGBToFloat(216), RGBToFloat(230), 1.0F);
        ImGui.SetWindowSize(200.0F, 200.0F);
        if (ImGui.Begin("Snows Constructor", 0)) {
            ImGui.PushStyleVar(11, 50.0F, 5.0F);
            if (this.isScriptRunning) {
                if (ImGui.Button("Stop Script")) {
                    this.script.stopScript();
                    this.isScriptRunning = false;
                }
            } else if (ImGui.Button("Start Script")) {
                this.script.startScript();
                this.isScriptRunning = true;
            }

            ImGui.PopStyleVar(3);
            ImGui.Separator();
            this.script.LightModeEnabled = ImGui.Checkbox("Light Mode", this.script.LightModeEnabled);
            ImGui.SameLine();
            ImGui.Text("(cuts out a lot of stuff to make it run faster!)", new Object[0]);
            boolean isMakeFromPlank;
            boolean isProteanPlank;
            if (!this.script.LightModeEnabled) {
                ImGui.Separator();
                ImGui.Text("Delay Mode:", new Object[0]);
                isMakeFromPlank = "Fast Method".equals(this.script.getDelayMode());
                if (ImGui.Checkbox("Fastest", isMakeFromPlank)) {
                    this.script.setDelayMode("Fast Method");
                }

                ImGui.SameLine();
                isProteanPlank = "Active".equals(this.script.getDelayMode());
                if (ImGui.Checkbox("Active", isProteanPlank)) {
                    this.script.setDelayMode("Active");
                }

                ImGui.SameLine();
                boolean isIntermediate = "Intermediate".equals(this.script.getDelayMode());
                if (ImGui.Checkbox("Intermediate", isIntermediate)) {
                    this.script.setDelayMode("Intermediate");
                }

                ImGui.SameLine();
                boolean isAFK = "AFK".equals(this.script.getDelayMode());
                if (ImGui.Checkbox("AFK", isAFK)) {
                    this.script.setDelayMode("AFK");
                }
            }

            ImGui.SeparatorText("Select Construction Item:");
            isMakeFromPlank = "Make from Plank".equals(this.script.getConstructionItem());
            if (ImGui.Checkbox("Make from Plank", isMakeFromPlank)) {
                this.script.setConstructionItem("Make from Plank");
            }

            ImGui.SameLine();
            isProteanPlank = "Protean Planks".equals(this.script.getConstructionItem());
            if (ImGui.Checkbox("Protean Planks", isProteanPlank)) {
                this.script.setConstructionItem("Protean Planks");
            }

            if (!this.script.LightModeEnabled && isMakeFromPlank && !isProteanPlank) {
                ImGui.Text("Select Flatpack Type:", new Object[0]);
                if (ImGui.Checkbox("Parlour Flatpacks", this.script.getSelectedFlatpackType().equals("Parlour Flatpacks"))) {
                    this.script.setSelectedFlatpackType("Parlour Flatpacks");
                }

                if (ImGui.Checkbox("Kitchen flatpacks", this.script.getSelectedFlatpackType().equals("Kitchen flatpacks"))) {
                    this.script.setSelectedFlatpackType("Kitchen flatpacks");
                }

                if (ImGui.Checkbox("Dining Room Flatpacks", this.script.getSelectedFlatpackType().equals("Dining Room Flatpacks"))) {
                    this.script.setSelectedFlatpackType("Dining Room Flatpacks");
                }

                if (ImGui.Checkbox("Bedroom Flatpacks", this.script.getSelectedFlatpackType().equals("Bedroom Flatpacks"))) {
                    this.script.setSelectedFlatpackType("Bedroom Flatpacks");
                }

                if (ImGui.Checkbox("Costume Flatpacks", this.script.getSelectedFlatpackType().equals("Costume Flatpacks"))) {
                    this.script.setSelectedFlatpackType("Costume Flatpacks");
                }
            }

            long elapsedTimeMillis = System.currentTimeMillis() - this.scriptStartTime;
            long elapsedSeconds = elapsedTimeMillis / 1000L;
            long hours = elapsedSeconds / 3600L;
            long minutes = elapsedSeconds % 3600L / 60L;
            long seconds = elapsedSeconds % 60L;
            String displayTimeRunning = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            ImGui.SeparatorText("Time Running  " + displayTimeRunning);
            int currentLevel = Skills.CONSTRUCTION.getSkill().getLevel();
            int levelsGained = currentLevel - this.startingConstructionLevel;
            ImGui.Text("Current Construction Level: " + currentLevel + "  (" + levelsGained + " Gained)", new Object[0]);
            int currentXP = Skills.CONSTRUCTION.getSkill().getExperience();
            currentLevel = Skills.CONSTRUCTION.getSkill().getLevel();
            int xpForNextLevel = Skills.CONSTRUCTION.getExperienceAt(currentLevel + 1);
            int xpTillNextLevel = xpForNextLevel - currentXP;
            ImGui.Text("XP remaining: " + xpTillNextLevel, new Object[0]);
            this.displayXPGained(Skills.CONSTRUCTION);
            this.displayXpPerHour(Skills.CONSTRUCTION);
            String timeToLevelStr = this.calculateTimeTillNextLevel();
            ImGui.Text(timeToLevelStr, new Object[0]);
            ImGui.Separator();
            this.displayXpProgressBar();
            ImGui.PopStyleColor(100);
            ImGui.Separator();
            ImGui.PushStyleColor(0, RGBToFloat(255), RGBToFloat(255), RGBToFloat(255), 0.7F);
            ImGui.Text("Script Information:", new Object[0]);
            ImGui.Text("Will only work with a portable workbench nearby", new Object[0]);
            ImGui.Text("mats in your bank preset/inventory if using protean planks", new Object[0]);
            ImGui.Text("Will logout if no planks/portables are found", new Object[0]);
            ImGui.Text("Will deploy Portable Workbench if in Inventory & Actionbar", new Object[0]);
            ImGui.Text("LightMode will not Deploy Portables!", new Object[0]);
            ImGui.End();
        }

        ImGui.PopStyleColor(1);
    }

    private void displayXPGained(Skills skill) {
        int currentXP = skill.getSkill().getExperience();
        int xpGained = currentXP - this.startingXP;
        ImGui.Text("XP Gained: " + xpGained, new Object[0]);
    }

    private void displayXpPerHour(Skills skill) {
        long elapsedTime = System.currentTimeMillis() - this.scriptStartTime;
        double hoursElapsed = (double)elapsedTime / 3600000.0;
        int currentXP = skill.getSkill().getExperience();
        int xpGained = currentXP - this.startingXP;
        double xpPerHour = hoursElapsed > 0.0 ? (double)xpGained / hoursElapsed : 0.0;
        String formattedXpPerHour = this.formatNumberForDisplay(xpPerHour);
        ImGui.Text("XP Per Hour: " + formattedXpPerHour, new Object[0]);
    }

    private String formatNumberForDisplay(double number) {
        if (number < 1000.0) {
            return String.format("%.0f", number);
        } else if (number < 1000000.0) {
            return String.format("%.1fk", number / 1000.0);
        } else {
            return number < 1.0E9 ? String.format("%.1fM", number / 1000000.0) : String.format("%.1fB", number / 1.0E9);
        }
    }

    private void displayTimeRunning() {
        long elapsedTimeMillis = System.currentTimeMillis() - this.scriptStartTime;
        long elapsedSeconds = elapsedTimeMillis / 1000L;
        long hours = elapsedSeconds / 3600L;
        long minutes = elapsedSeconds % 3600L / 60L;
        long seconds = elapsedSeconds % 60L;
        String timeRunningFormatted = String.format("Time Running: %02d:%02d:%02d", hours, minutes, seconds);
        ImGui.Text(timeRunningFormatted, new Object[0]);
    }

    private static float RGBToFloat(int rgbValue) {
        return (float)rgbValue / 255.0F;
    }

    private void displayXpProgressBar() {
        int currentXP = Skills.CONSTRUCTION.getSkill().getExperience();
        int currentLevel = Skills.CONSTRUCTION.getSkill().getLevel();
        int xpForNextLevel = Skills.CONSTRUCTION.getExperienceAt(currentLevel + 1);
        int xpForCurrentLevel = Skills.CONSTRUCTION.getExperienceAt(currentLevel);
        int xpToNextLevel = xpForNextLevel - xpForCurrentLevel;
        int xpGainedTowardsNextLevel = currentXP - xpForCurrentLevel;
        float progress = (float)xpGainedTowardsNextLevel / (float)xpToNextLevel;
        float[][] colors = new float[][]{{1.0F, 0.0F, 0.0F, 1.0F}, {1.0F, 0.4F, 0.4F, 1.0F}, {1.0F, 0.6F, 0.0F, 1.0F}, {1.0F, 0.7F, 0.4F, 1.0F}, {1.0F, 1.0F, 0.0F, 1.0F}, {0.8F, 1.0F, 0.4F, 1.0F}, {0.6F, 1.0F, 0.6F, 1.0F}, {0.4F, 1.0F, 0.4F, 1.0F}, {0.3F, 0.9F, 0.3F, 1.0F}, {0.2F, 0.8F, 0.2F, 1.0F}, {0.1F, 0.7F, 0.1F, 1.0F}};
        int index = (int)(progress * 10.0F);
        float blend = progress * 10.0F - (float)index;
        if (index >= colors.length - 1) {
            index = colors.length - 2;
            blend = 1.0F;
        }

        float[] startColor = colors[index];
        float[] endColor = colors[index + 1];
        float[] currentColor = new float[]{startColor[0] + blend * (endColor[0] - startColor[0]), startColor[1] + blend * (endColor[1] - startColor[1]), startColor[2] + blend * (endColor[2] - startColor[2]), 1.0F};
        ImGui.PushStyleColor(42, currentColor[0], currentColor[1], currentColor[2], currentColor[3]);
        ImGui.SeparatorText("XP Progress to Next Level:");
        ImGui.PushStyleColor(0, RGBToFloat(0), RGBToFloat(0), RGBToFloat(0), 0.0F);
        ImGui.ProgressBar(String.format("%.2f%%", progress * 100.0F), progress, 200.0F, 15.0F);
        ImGui.PopStyleColor(2);
    }

    private String calculateTimeTillNextLevel() {
        int currentXP = Skills.CONSTRUCTION.getSkill().getExperience();
        int currentLevel = Skills.CONSTRUCTION.getSkill().getLevel();
        int xpForNextLevel = Skills.CONSTRUCTION.getExperienceAt(currentLevel + 1);
        int xpForCurrentLevel = Skills.CONSTRUCTION.getExperienceAt(currentLevel);
        int var10000 = currentXP - xpForCurrentLevel;
        long currentTime = System.currentTimeMillis();
        int xpGained = currentXP - this.startingXP;
        long timeElapsed = currentTime - this.scriptStartTime;
        if (xpGained > 0 && timeElapsed > 0L) {
            double xpPerMillisecond = (double)xpGained / (double)timeElapsed;
            long timeToLevelMillis = (long)((double)(xpForNextLevel - currentXP) / xpPerMillisecond);
            long timeToLevelSecs = timeToLevelMillis / 1000L;
            long hours = timeToLevelSecs / 3600L;
            long minutes = timeToLevelSecs % 3600L / 60L;
            long seconds = timeToLevelSecs % 60L;
            return String.format("Time to level: %02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return "Time to level: calculating...";
        }
    }

    private void XPtillNextLevel() {
        int currentXP = Skills.CONSTRUCTION.getSkill().getExperience();
        int currentLevel = Skills.CONSTRUCTION.getSkill().getLevel();
        int xpForNextLevel = Skills.CONSTRUCTION.getExperienceAt(currentLevel + 1);
        int var10000 = xpForNextLevel - currentXP;
    }
}
