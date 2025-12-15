package com.oheers.fish.messages;

import com.oheers.fish.config.MessageConfig;

public enum PrefixType {

    NONE(null, null),
    ADMIN("prefix-admin", "<red>[EvenMoreFish] "),
    DEFAULT("prefix-regular", "<green>[EvenMoreFish] "),
    ERROR("prefix-error", "<red>[EvenMoreFish] ");

    private final String id, normal;

    /**
     * This contains the id and normal reference to the prefixes. These can be obtained through the getPrefix() method.
     *
     * @param id     The config id for the prefix.
     * @param normal The default values for the prefix.
     */
    PrefixType(final String id, final String normal) {
        this.id = id;
        this.normal = normal;
    }

    /**
     * Gives the associated prefix message.
     * If the PrefixType is NONE, then an empty message is returned.
     *
     * @return An EMFSingleMessage containing the prefix, or empty if the type is NONE.
     */
    public EMFSingleMessage getPrefix() {
        if (id == null) {
            return EMFSingleMessage.empty();
        } else {
            return EMFSingleMessage.fromString(MessageConfig.getInstance().getConfig().getString(id, normal));
        }
    }
}
