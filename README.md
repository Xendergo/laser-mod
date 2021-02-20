# Laser Mod

## Lasers

Lasers have two properties, power and frequency. The power changes the strength of the laser and is represented by the laser's opacity, and is a number between 0 & 15. The frequency is what gives lasers cool properties, and is between 0 & 16. Entities can also block the laser's path. If two lasers are in the same block, the colors will blend.

These are the properties of different frequencies:
### 0-1, microwaves
Microwave lasers are rendered as red & can set stuff on fire.

### 1-2, infrared
Infrared lasers aren't rendered at all, useful as tripwire

### 2-14, visible
Useful for nice decorations, the color depends on the frequency set

### 14-15, UV
Totally transparent like infrared, but does damage to living entities

### 15-16, Gamma rays
Rendered as white, does a lot of damage to living entities, and breaks blocks

When a laser goes into a lens, beam splitter, or coupler, the power is reduced by 1. (this is for technical reasons, otherwise you could easily make infinite loops of lasers and crash your game)

## Blocks

### Laser
Outputs a laser with a specific frequency and power. Power is determined by the redstone power level inputted to the block. You can set the frequency by using your arrow keys while looking at the block, or by pointing a comparator directly into the block. (Through a block won't work)

### Lens
Redirects a laser in the direction the lens is facing, pointing a laser into the output redirects it in all other directions.

### Beam splitter
Splits a laser into the direction it was travelling, and the direction the beam splitter is facing.

### Laser detector
Pointing a laser into the laser detector makes it output a redstone signal. The signal strength is proportional to the power. It also has a comparator output, which is the frequency of the laser. The comparator output of the laser detector is always equal to the comparator input of the laser. In the case that laser detector receives multiple lasers, the signal strength is the power of the most powerful laser, and the comparator output is the frequency of the most powerful laser.

### Fiber optic cable
Allows you to send lasers over infinite distance with minimal lag, if you're willing to place the blocks.

### Coupler
Couples light into and out of fiber optic cables. When the lasers inputted to the coupler changes, the coupler on the other side is loaded for 600 ticks (30 seconds). The ticket has a level of 31.

## Crafting recipes

```
B: Blackstone
L: Lens
T: Glass bottle
I: Iron
G: Glass
Q: Quartz
D: Daylight detector
```

### Laser
```
B|L|B
B|T|B
B|I|B
```

### Lens
```
B|G|B
G|Q|G
B|G|B
```

### Beam splitter
```
B|G|B
G|I|G
B|G|B
```

### Laser detector
```
B|B|B
Q|D|B
B|B|B
```

### Fiber optic cable
```
B|B|B
G|G|G
B|B|B
```
(You get 9 per craft)

### Coupler
```
B|B|B
L|T|G
B|B|B
```