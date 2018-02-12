import com.jj.clipper.ClipperCardParser

final CliBuilder cli = new CliBuilder(usage: "ClipperCard.groovy -file <pdf>")
cli.with {
    h longOpt: 'help', 'Show usage'
    f longOpt: 'file', args: 1, argName: 'pdf', 'Clipper transaction history PDF file; Defaults to "~/Downloads/ridehistory.pdf"'
    l longOpt: 'limit', args: 1, argName: 'discrepancyAmountLimit', 'Ignore discrepancies whose absolute deviation is greater than the specified limit; Defaults to 50'
}

final OptionAccessor options = cli.parse(args)

if (!options) {
    return
} else if (options.help) {
    cli.usage()
    return
}

final File pdfFile = getPdfFile(options)
ensureCanReadFileElseExit(pdfFile)

final BigDecimal discrepancyAmountLimit = getDiscrepancyAmountLimit(options)

final ClipperCardParser clipperCardParser = new ClipperCardParser()
clipperCardParser.parsePdfFile(pdfFile, discrepancyAmountLimit)

private static File getPdfFile(final OptionAccessor options) {
    if (options.file) {
        return new File(options.file as String)
    } else {
        final File userHomeDir = new File(System.getProperty('user.home'))
        return new File(userHomeDir, 'Downloads/ridehistory.pdf')
    }
}

private static void ensureCanReadFileElseExit(final File file) {
    if (!file.canRead()) {
        System.err.println "File '${file.canonicalPath}' does not exist or is not readable"
        System.exit(1)
    }
}

private static BigDecimal getDiscrepancyAmountLimit(final OptionAccessor options) {
    return (options.limit ?: 50) as BigDecimal
}
