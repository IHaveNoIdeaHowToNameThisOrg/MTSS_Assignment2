from jinja2 import Environment, FileSystemLoader
from typing import TextIO
import glob
import os
import os.path

OS_GRID = {
    "ubuntu": {
        "name": "Ubuntu",
        "link": "https://github.com/actions/virtual-environments/blob/main/images/linux/Ubuntu2004-Readme.md",
    },
    "windows": {
        "name": "Windows",
        "link": "https://github.com/actions/virtual-environments/blob/main/images/win/Windows2022-Readme.md",
    },
    "macos": {
        "name": "macOS",
        "link": "https://github.com/actions/virtual-environments/blob/main/images/macos/macos-11-Readme.md",
    },
}

JDK_GRID = {
    "17": "JDK 17 LTS",
    "18": "JDK 18",
}

PWD = os.path.dirname(os.path.realpath(__file__))
OUT_DIR = os.path.join(PWD, "..", "..", ".github", "workflows")

env = Environment(loader=FileSystemLoader(PWD))

tpl = env.get_template("ci_template.yml.j2")

for file in glob.glob(os.path.join(OUT_DIR, "maven_*.yml")):
    os.remove(file)


def get_file_name(ci_os: str, ci_jdk: str) -> str:
    return f"maven_{ci_os}_jdk{ci_jdk}.yml"


for ci_os in OS_GRID:
    for ci_jdk in JDK_GRID:
        with open(os.path.join(OUT_DIR, get_file_name(ci_os, ci_jdk)), "w") as out_file:
            out_file.write(tpl.render(os=ci_os, jdk=ci_jdk))


def write_ci_matrix_table(f: TextIO) -> None:
    f.write("|    |")
    for osk, osv in OS_GRID.items():
        f.write(f" [{osv['name']}]({osv['link']}) |")
    f.write("\n")
    f.write("|----|")
    for _ in OS_GRID:
        f.write(":---:|")
    f.write("\n")
    for jk, jv in JDK_GRID.items():
        f.write(F"| {jv} |")
        for osk in OS_GRID:
            f.write("[![Java CI](https://github.com/IHaveNoIdeaHowToNameThisOrg/MTSS_Assignment2/actions"
                    f"/workflows/{get_file_name(osk, jk)}/badge.svg?branch=develop)](https://github.com/"
                    f"IHaveNoIdeaHowToNameThisOrg/MTSS_Assignment2/actions/workflows/{get_file_name(osk, jk)}) |"
                    )
        f.write("\n")


with open(os.path.join(PWD, "..", "..", "README.md")) as old_readme:
    old_readme_text = old_readme.readlines()

BEGIN_TABLE_MARKER = "<!-- ciMatrixStart -->\n"
END_TABLE_MARKER = "<!-- ciMatrixEnd -->\n"

with open(os.path.join(PWD, "..", "..", "README.md"), "w") as new_readme:
    found_begin_marker = False
    for line in old_readme_text:
        if line == END_TABLE_MARKER:
            found_begin_marker = False
        if not found_begin_marker:
            new_readme.write(line)
        if line == BEGIN_TABLE_MARKER:
            found_begin_marker = True
            write_ci_matrix_table(new_readme)
