package net.botwithus;

import java.time.Instant;
import net.botwithus.api.game.hud.Dialog;
import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.internal.scripts.ScriptDefinition;
import net.botwithus.rs3.game.actionbar.ActionBar;
import net.botwithus.rs3.game.hud.interfaces.Component;
import net.botwithus.rs3.game.hud.interfaces.Interfaces;
import net.botwithus.rs3.game.js5.types.vars.VarDomainType;
import net.botwithus.rs3.game.minimenu.MiniMenu;
import net.botwithus.rs3.game.minimenu.actions.ComponentAction;
import net.botwithus.rs3.game.queries.builders.components.ComponentQuery;
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery;
import net.botwithus.rs3.game.scene.entities.object.SceneObject;
import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.game.vars.VarManager;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.script.LoopingScript;
import net.botwithus.rs3.script.config.ScriptConfig;
import net.botwithus.rs3.util.RandomGenerator;

public class SkeletonScript extends LoopingScript {
    private boolean scriptRunning = false;
    private Instant scriptStartTime;
    private int startingXP;
    private String constructionItem = "";
    private String selectedFlatpackType = "";
    private String delayMode = "Fast Method";
    private boolean portableWorkbenchDeployed = false;
    boolean LightModeEnabled = true;
    private boolean bankChestInteracted = false;
    private boolean workbenchInteractionComplete = false;
    private final boolean flatpacksCreationInProgress = false;
    private int startingConstructionLevel;
    private SceneObject cachedWorkbench = null;
    private SceneObject cachedBankChest = null;
    private ScriptState currentState;

    public int getStartingConstructionLevel() {
        return this.startingConstructionLevel;
    }

    public String getDelayMode() {
        return this.delayMode;
    }

    public void setDelayMode(String delayMode) {
        this.delayMode = delayMode;
    }

    public boolean initialize() {
        this.startingConstructionLevel = Skills.CONSTRUCTION.getSkill().getLevel();
        this.sgc = new SkeletonScriptGraphicsContext(this.getConsole(), this);
        this.loopDelay = 1000L;
        return super.initialize();
    }

    public SkeletonScript(String name, ScriptConfig scriptConfig, ScriptDefinition scriptDefinition) {
        super(name, scriptConfig, scriptDefinition);
        this.currentState = SkeletonScript.ScriptState.INTERACTING_WITH_BANK;
    }

    public void startScript() {
        if (!this.scriptRunning) {
            this.scriptRunning = true;
            this.scriptStartTime = Instant.now();
            this.println("Script started.");
            this.currentState = SkeletonScript.ScriptState.INTERACTING_WITH_BANK;
        } else {
            this.println("Script is already running.");
        }

    }

    public void stopScript() {
        if (this.scriptRunning) {
            this.scriptRunning = false;
            this.println("Script stopped.");
        } else {
            this.println("Script is not running.");
        }

    }

    public String getConstructionItem() {
        return this.constructionItem;
    }

    public void setConstructionItem(String constructionItem) {
        this.constructionItem = constructionItem;
    }

    public void onLoop() {
        if (this.scriptRunning) {
            if (this.LightModeEnabled) {
                SceneObject portableworkbench;
                if (!"Protean Planks".equals(this.constructionItem)) {
                    portableworkbench = (SceneObject)SceneObjectQuery.newQuery().name("Bank chest").results().nearest();
                    if (portableworkbench != null && portableworkbench.interact("Load Last Preset from")) {
                        this.println("Interacted with Bank Chest to load last preset.");
                        Execution.delay((long)RandomGenerator.nextInt(200, 250));
                        Execution.delayUntil(5000L, Backpack::isFull);
                        Execution.delay((long)RandomGenerator.nextInt(200, 250));
                        if (this.checkLogoutConditions()) {
                            this.println("You have run out of planks/no portable nearby. Logging out.");
                            this.stopScript();
                            this.performLogout();
                        }
                    }
                }

                portableworkbench = (SceneObject)SceneObjectQuery.newQuery().name("Portable workbench").results().nearest();
                if (portableworkbench != null) {
                    portableworkbench.interact("Make Flatpacks");
                    this.println("Started making Flatpacks at Portable Workbench.");
                    boolean interfaceOpened = Execution.delayUntil(5000L, () -> {
                        return Interfaces.isOpen(1370);
                    });
                    Execution.delay((long)RandomGenerator.nextInt(600, 700));
                    if (interfaceOpened) {
                        MiniMenu.interact(ComponentAction.DIALOGUE.getType(), 0, -1, 89784350);
                        Execution.delay((long)RandomGenerator.nextInt(600, 700));
                        this.println("Waiting for completion.");
                        if (Execution.delayUntil(360000L, () -> {
                            return !Interfaces.isOpen(1251);
                        })) {
                            Execution.delay((long)RandomGenerator.nextInt(600, 700));
                            return;
                        }
                    }
                }
            } else {
                switch (this.currentState) {
                    case INTERACTING_WITH_BANK:
                        if ("Protean Planks".equals(this.constructionItem)) {
                            this.println("Using Protean Planks. Skipping bank interaction.");
                            this.currentState = SkeletonScript.ScriptState.DEPLOYING_WORKBENCH;
                        } else {
                            if (this.cachedBankChest == null || !this.cachedBankChest.validate()) {
                                this.cachedBankChest = (SceneObject)SceneObjectQuery.newQuery().name("Bank chest").results().nearest();
                            }

                            if (this.cachedBankChest != null && this.interactWithBankChest()) {
                                this.currentState = SkeletonScript.ScriptState.DEPLOYING_WORKBENCH;
                            }
                        }
                        break;
                    case DEPLOYING_WORKBENCH:
                        if (this.deployPortableWorkbench()) {
                            this.println("Portable Workbench deployed or already available.");
                            this.currentState = SkeletonScript.ScriptState.CHECK_AND_LOGOUT_IF_NEEDED;
                        } else {
                            this.println("Failed to deploy Portable Workbench. Checking logout conditions.");
                            this.currentState = SkeletonScript.ScriptState.CHECK_AND_LOGOUT_IF_NEEDED;
                        }
                        break;
                    case CHECK_AND_LOGOUT_IF_NEEDED:
                        if (this.checkLogoutConditions()) {
                            boolean loggedOut = this.performLogout();
                            if (loggedOut) {
                                this.println("Successfully logged out.");
                                this.scriptRunning = false;
                            } else {
                                this.println("Failed to log out.");
                            }
                        } else {
                            this.currentState = SkeletonScript.ScriptState.MAKING_FLATPACKS;
                        }
                        break;
                    case MAKING_FLATPACKS:
                        if (this.interactWithPortableWorkbench()) {
                            this.currentState = SkeletonScript.ScriptState.INTERACTWITHCONSTRUCT;
                        }
                        break;
                    case INTERACTWITHCONSTRUCT:
                        if (this.InteractWithDialog()) {
                            this.currentState = SkeletonScript.ScriptState.WAITING_FOR_COMPLETION;
                        }
                        break;
                    case WAITING_FOR_COMPLETION:
                        if (this.waitForFlatpackCreationToComplete()) {
                            this.currentState = SkeletonScript.ScriptState.INTERACTING_WITH_BANK;
                        }
                }
            }

        }
    }

    private boolean deployPortableWorkbench() {
        boolean workbenchNearby = SceneObjectQuery.newQuery().name("Portable workbench").results().nearest() != null;
        boolean hasPortableWorkbenchInBackpack = Backpack.contains(new String[]{"Portable workbench"});
        if (!workbenchNearby && hasPortableWorkbenchInBackpack) {
            this.println("No Portable Workbench found nearby. Deploying one from backpack.");
            ActionBar.useItem("Portable workbench", "Deploy");
            Execution.delayUntil(5000L, () -> {
                return Interfaces.isOpen(1188);
            });
            this.customDelay();
            Dialog.interact("Yes.");
            this.customDelay();
            this.portableWorkbenchDeployed = true;
        } else if (workbenchNearby) {
            this.println("Portable Workbench is already nearby.");
            this.portableWorkbenchDeployed = true;
        } else {
            this.println("No Portable Workbench found in backpack. Unable to deploy.");
            this.portableWorkbenchDeployed = false;
        }

        return this.portableWorkbenchDeployed;
    }

    private boolean interactWithBankChest() {
        SceneObject bankChest = (SceneObject)SceneObjectQuery.newQuery().name("Bank chest").results().nearest();
        if (bankChest != null && bankChest.interact("Load Last Preset from")) {
            this.println("Interacted with Bank Chest to load last preset.");
            this.customDelay();
            this.bankChestInteracted = true;
            return true;
        } else {
            return false;
        }
    }

    private boolean interactWithPortableWorkbench() {
        this.customDelay();
        SceneObject workbench = (SceneObject)SceneObjectQuery.newQuery().name("Portable workbench").results().nearest();
        if (workbench != null) {
            boolean interfaceOpened;
            if (!"Protean Planks".equals(this.constructionItem)) {
                if (workbench.interact("Make Flatpacks")) {
                    this.println("Started making Flatpacks at Portable Workbench.");
                    interfaceOpened = Execution.delayUntil(5000L, () -> {
                        return Interfaces.isOpen(1370);
                    });
                    if (interfaceOpened) {
                        this.handleFlatpackSelection();
                        return true;
                    }

                    this.println("Failed to open the flatpack making interface.");
                    return false;
                }
            } else if (workbench.interact("Make Flatpacks")) {
                this.println("Interacted with Portable Workbench for Protean Planks.");
                interfaceOpened = Execution.delayUntil(5000L, () -> {
                    return Interfaces.isOpen(1370);
                });
                if (interfaceOpened) {
                    return true;
                }

                this.println("Failed to initiate interaction with Portable Workbench for Protean Planks.");
                return false;
            }
        } else {
            this.println("Could not find Portable Workbench.");
        }

        return false;
    }

    private void handleFlatpackSelection() {
        switch (this.getSelectedFlatpackType()) {
            case "Parlour Flatpacks":
                this.interactWithComponentForParlourFlatpacks();
                break;
            case "Kitchen Flatpacks":
                this.interactWithComponentForKitchenFlatpacks();
                break;
            case "Dining Room Flatpacks":
                this.interactWithComponentForDiningRoomFlatpacks();
                break;
            case "Bedroom Flatpacks":
                this.interactWithComponentForBedroomFlatpacks();
                break;
            case "Costume Flatpacks":
                this.interactWithComponentForCostumeFlatpacks();
                break;
            default:
                this.println("Selected flatpack type is not recognized.");
        }

    }

    public boolean InteractWithDialog() {
        if (Interfaces.isOpen(1370) && VarManager.getVarValue(VarDomainType.PLAYER, 8847) > 0) {
            MiniMenu.interact(ComponentAction.DIALOGUE.getType(), 0, -1, 89784350);
            this.println("Pressed Construct and waiting for completion.");
            return Execution.delayUntil(3600000L, () -> {
                return !Interfaces.isOpen(1251);
            });
        } else {
            this.println("The required interface is not open or the variable condition is not met.");
            return false;
        }
    }

    public boolean waitForFlatpackCreationToComplete() {
        return Execution.delayUntil(3600000L, () -> {
            return !Interfaces.isOpen(1251);
        });
    }

    public String getSelectedFlatpackType() {
        return this.selectedFlatpackType;
    }

    public void setSelectedFlatpackType(String selectedFlatpackType) {
        this.selectedFlatpackType = selectedFlatpackType;
    }

    private void interactWithComponentForParlourFlatpacks() {
        if (VarManager.getVarValue(VarDomainType.PLAYER, 1169) != 11347) {
            Component button = (Component)ComponentQuery.newQuery(new int[]{1371}).componentIndex(new int[]{28}).results().first();
            if (button != null) {
                button.interact(1);
                Execution.delay((long)RandomGenerator.nextInt(100, 200));
            }

            Component select = (Component)ComponentQuery.newQuery(new int[]{1477}).componentIndex(new int[]{886}).subComponentIndex(new int[]{1}).results().first();
            if (select != null) {
                select.interact(1);
                Execution.delay((long)RandomGenerator.nextInt(500, 600));
            }
        }

    }

    private void interactWithComponentForDiningRoomFlatpacks() {
        if (VarManager.getVarValue(VarDomainType.PLAYER, 1169) != 11349) {
            Component button = (Component)ComponentQuery.newQuery(new int[]{1371}).componentIndex(new int[]{28}).results().first();
            if (button != null) {
                button.interact(1);
                Execution.delay((long)RandomGenerator.nextInt(100, 200));
            }

            Component select = (Component)ComponentQuery.newQuery(new int[]{1477}).componentIndex(new int[]{886}).subComponentIndex(new int[]{5}).results().first();
            if (select != null) {
                select.interact(1);
                Execution.delay((long)RandomGenerator.nextInt(500, 600));
            }
        }

    }

    private void interactWithComponentForCostumeFlatpacks() {
        if (VarManager.getVarValue(VarDomainType.PLAYER, 1169) != 11351) {
            Component button = (Component)ComponentQuery.newQuery(new int[]{1371}).componentIndex(new int[]{28}).results().first();
            if (button != null) {
                button.interact(1);
                Execution.delay((long)RandomGenerator.nextInt(100, 200));
            }

            Component select = (Component)ComponentQuery.newQuery(new int[]{1477}).componentIndex(new int[]{886}).subComponentIndex(new int[]{9}).results().first();
            if (select != null) {
                select.interact(1);
                Execution.delay((long)RandomGenerator.nextInt(500, 600));
            }
        }

    }

    private void interactWithComponentForKitchenFlatpacks() {
        if (VarManager.getVarValue(VarDomainType.PLAYER, 1169) != 11348) {
            Component button = (Component)ComponentQuery.newQuery(new int[]{1371}).componentIndex(new int[]{28}).results().first();
            if (button != null) {
                button.interact(1);
                Execution.delay((long)RandomGenerator.nextInt(100, 200));
            }

            Component select = (Component)ComponentQuery.newQuery(new int[]{1477}).componentIndex(new int[]{886}).subComponentIndex(new int[]{3}).results().first();
            if (select != null) {
                select.interact(1);
                Execution.delay((long)RandomGenerator.nextInt(500, 600));
            }
        }

    }

    private void interactWithComponentForBedroomFlatpacks() {
        if (VarManager.getVarValue(VarDomainType.PLAYER, 1169) != 11350) {
            Component button = (Component)ComponentQuery.newQuery(new int[]{1371}).componentIndex(new int[]{28}).results().first();
            if (button != null) {
                button.interact(1);
                Execution.delay((long)RandomGenerator.nextInt(100, 200));
            }

            Component select = (Component)ComponentQuery.newQuery(new int[]{1477}).componentIndex(new int[]{886}).subComponentIndex(new int[]{7}).results().first();
            if (select != null) {
                select.interact(1);
                Execution.delay((long)RandomGenerator.nextInt(500, 600));
            }
        }

    }

    public boolean checkLogoutConditions() {
        int totalPlanks = Backpack.getCount(new String[]{"Plank", "Teak plank", "Mahogany plank", "Oak plank"});
        boolean hasProteanPlank = Backpack.contains(new String[]{"Protean plank"});
        boolean workbenchNearby = SceneObjectQuery.newQuery().name("Portable workbench").results().nearest() != null;
        boolean hasPortableWorkbenchInBackpack = Backpack.contains(new String[]{"Portable workbench"});
        if ((hasProteanPlank || totalPlanks >= 10) && (workbenchNearby || hasPortableWorkbenchInBackpack)) {
            return false;
        } else {
            this.println("Conditions met for logout.");
            return true;
        }
    }

    public boolean performLogout() {
        MiniMenu.interact(ComponentAction.COMPONENT.getType(), 1, 7, 93782016);
        if (Interfaces.isOpen(1433)) {
            Component logoutButton = (Component)ComponentQuery.newQuery(new int[]{1433}).componentIndex(new int[]{71}).results().first();
            if (logoutButton != null && logoutButton.interact(1)) {
                this.println("Logout initiated.");
                this.stopScript1();
                return true;
            } else {
                this.println("Could not find or interact with the logout button.");
                return false;
            }
        } else {
            this.println("Failed to open logout menu.");
            return false;
        }
    }

    private void stopScript1() {
        this.scriptRunning = false;
        this.println("Script stopping...");
    }

    private void customDelay() {
        int delayValue;
        switch (this.delayMode) {
            case "Active":
                delayValue = RandomGenerator.nextInt(180, 300);
                Execution.delay((long)delayValue);
                this.println("Active delay: " + delayValue + "ms");
                break;
            case "Intermediate":
                delayValue = RandomGenerator.nextInt(300, 1500);
                Execution.delay((long)delayValue);
                this.println("Intermediate delay: " + delayValue + "ms");
                break;
            case "AFK":
                delayValue = RandomGenerator.nextInt(1000, 10000);
                Execution.delay((long)delayValue);
                this.println("AFK delay: " + delayValue + "ms");
                break;
            default:
                delayValue = RandomGenerator.nextInt(50, 80);
                Execution.delay((long)delayValue);
                this.println("Fast Method: " + delayValue + "ms");
        }

    }

    public static enum ScriptState {
        INTERACTING_WITH_BANK,
        DEPLOYING_WORKBENCH,
        CHECK_AND_LOGOUT_IF_NEEDED,
        MAKING_FLATPACKS,
        INTERACTWITHCONSTRUCT,
        WAITING_FOR_COMPLETION,
        CYCLE_COMPLETE,
        FASTINTERACTWITHBANK;

        private ScriptState() {
        }
    }
}
//test
