// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'structure_diff.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StructureDiffCWProxy {
  StructureDiff format(StructureDiffFormatEnum format);

  StructureDiff fixtureFile(String? fixtureFile);

  StructureDiff addedPaths(List<String> addedPaths);

  StructureDiff removedPaths(List<String> removedPaths);

  StructureDiff typeChanged(List<StructureDiffTypeChangedInner> typeChanged);

  StructureDiff presenceDropped(
    List<StructureDiffPresenceDroppedInner>? presenceDropped,
  );

  StructureDiff arrayEmpty(List<String>? arrayEmpty);

  StructureDiff columnsReordered(bool? columnsReordered);

  StructureDiff delimiterChanged(
    StructureDiffDelimiterChanged? delimiterChanged,
  );

  StructureDiff contentTypeChanged(
    StructureDiffContentTypeChanged? contentTypeChanged,
  );

  StructureDiff sizeRatio(num? sizeRatio);

  StructureDiff renameCandidates(
    List<StructureDiffRenameCandidatesInner>? renameCandidates,
  );

  StructureDiff htmlTitle(String? htmlTitle);

  StructureDiff examples(Map<String, List<String>>? examples);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StructureDiff(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StructureDiff(...).copyWith(id: 12, name: "My name")
  /// ```
  StructureDiff call({
    StructureDiffFormatEnum format,
    String? fixtureFile,
    List<String> addedPaths,
    List<String> removedPaths,
    List<StructureDiffTypeChangedInner> typeChanged,
    List<StructureDiffPresenceDroppedInner>? presenceDropped,
    List<String>? arrayEmpty,
    bool? columnsReordered,
    StructureDiffDelimiterChanged? delimiterChanged,
    StructureDiffContentTypeChanged? contentTypeChanged,
    num? sizeRatio,
    List<StructureDiffRenameCandidatesInner>? renameCandidates,
    String? htmlTitle,
    Map<String, List<String>>? examples,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStructureDiff.copyWith(...)` or call `instanceOfStructureDiff.copyWith.fieldName(value)` for a single field.
class _$StructureDiffCWProxyImpl implements _$StructureDiffCWProxy {
  const _$StructureDiffCWProxyImpl(this._value);

  final StructureDiff _value;

  @override
  StructureDiff format(StructureDiffFormatEnum format) => call(format: format);

  @override
  StructureDiff fixtureFile(String? fixtureFile) =>
      call(fixtureFile: fixtureFile);

  @override
  StructureDiff addedPaths(List<String> addedPaths) =>
      call(addedPaths: addedPaths);

  @override
  StructureDiff removedPaths(List<String> removedPaths) =>
      call(removedPaths: removedPaths);

  @override
  StructureDiff typeChanged(List<StructureDiffTypeChangedInner> typeChanged) =>
      call(typeChanged: typeChanged);

  @override
  StructureDiff presenceDropped(
    List<StructureDiffPresenceDroppedInner>? presenceDropped,
  ) => call(presenceDropped: presenceDropped);

  @override
  StructureDiff arrayEmpty(List<String>? arrayEmpty) =>
      call(arrayEmpty: arrayEmpty);

  @override
  StructureDiff columnsReordered(bool? columnsReordered) =>
      call(columnsReordered: columnsReordered);

  @override
  StructureDiff delimiterChanged(
    StructureDiffDelimiterChanged? delimiterChanged,
  ) => call(delimiterChanged: delimiterChanged);

  @override
  StructureDiff contentTypeChanged(
    StructureDiffContentTypeChanged? contentTypeChanged,
  ) => call(contentTypeChanged: contentTypeChanged);

  @override
  StructureDiff sizeRatio(num? sizeRatio) => call(sizeRatio: sizeRatio);

  @override
  StructureDiff renameCandidates(
    List<StructureDiffRenameCandidatesInner>? renameCandidates,
  ) => call(renameCandidates: renameCandidates);

  @override
  StructureDiff htmlTitle(String? htmlTitle) => call(htmlTitle: htmlTitle);

  @override
  StructureDiff examples(Map<String, List<String>>? examples) =>
      call(examples: examples);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StructureDiff(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StructureDiff(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StructureDiff call({
    Object? format = const $CopyWithPlaceholder(),
    Object? fixtureFile = const $CopyWithPlaceholder(),
    Object? addedPaths = const $CopyWithPlaceholder(),
    Object? removedPaths = const $CopyWithPlaceholder(),
    Object? typeChanged = const $CopyWithPlaceholder(),
    Object? presenceDropped = const $CopyWithPlaceholder(),
    Object? arrayEmpty = const $CopyWithPlaceholder(),
    Object? columnsReordered = const $CopyWithPlaceholder(),
    Object? delimiterChanged = const $CopyWithPlaceholder(),
    Object? contentTypeChanged = const $CopyWithPlaceholder(),
    Object? sizeRatio = const $CopyWithPlaceholder(),
    Object? renameCandidates = const $CopyWithPlaceholder(),
    Object? htmlTitle = const $CopyWithPlaceholder(),
    Object? examples = const $CopyWithPlaceholder(),
  }) {
    return StructureDiff(
      format: format == const $CopyWithPlaceholder() || format == null
          ? _value.format
          // ignore: cast_nullable_to_non_nullable
          : format as StructureDiffFormatEnum,
      fixtureFile: fixtureFile == const $CopyWithPlaceholder()
          ? _value.fixtureFile
          // ignore: cast_nullable_to_non_nullable
          : fixtureFile as String?,
      addedPaths:
          addedPaths == const $CopyWithPlaceholder() || addedPaths == null
          ? _value.addedPaths
          // ignore: cast_nullable_to_non_nullable
          : addedPaths as List<String>,
      removedPaths:
          removedPaths == const $CopyWithPlaceholder() || removedPaths == null
          ? _value.removedPaths
          // ignore: cast_nullable_to_non_nullable
          : removedPaths as List<String>,
      typeChanged:
          typeChanged == const $CopyWithPlaceholder() || typeChanged == null
          ? _value.typeChanged
          // ignore: cast_nullable_to_non_nullable
          : typeChanged as List<StructureDiffTypeChangedInner>,
      presenceDropped: presenceDropped == const $CopyWithPlaceholder()
          ? _value.presenceDropped
          // ignore: cast_nullable_to_non_nullable
          : presenceDropped as List<StructureDiffPresenceDroppedInner>?,
      arrayEmpty: arrayEmpty == const $CopyWithPlaceholder()
          ? _value.arrayEmpty
          // ignore: cast_nullable_to_non_nullable
          : arrayEmpty as List<String>?,
      columnsReordered: columnsReordered == const $CopyWithPlaceholder()
          ? _value.columnsReordered
          // ignore: cast_nullable_to_non_nullable
          : columnsReordered as bool?,
      delimiterChanged: delimiterChanged == const $CopyWithPlaceholder()
          ? _value.delimiterChanged
          // ignore: cast_nullable_to_non_nullable
          : delimiterChanged as StructureDiffDelimiterChanged?,
      contentTypeChanged: contentTypeChanged == const $CopyWithPlaceholder()
          ? _value.contentTypeChanged
          // ignore: cast_nullable_to_non_nullable
          : contentTypeChanged as StructureDiffContentTypeChanged?,
      sizeRatio: sizeRatio == const $CopyWithPlaceholder()
          ? _value.sizeRatio
          // ignore: cast_nullable_to_non_nullable
          : sizeRatio as num?,
      renameCandidates: renameCandidates == const $CopyWithPlaceholder()
          ? _value.renameCandidates
          // ignore: cast_nullable_to_non_nullable
          : renameCandidates as List<StructureDiffRenameCandidatesInner>?,
      htmlTitle: htmlTitle == const $CopyWithPlaceholder()
          ? _value.htmlTitle
          // ignore: cast_nullable_to_non_nullable
          : htmlTitle as String?,
      examples: examples == const $CopyWithPlaceholder()
          ? _value.examples
          // ignore: cast_nullable_to_non_nullable
          : examples as Map<String, List<String>>?,
    );
  }
}

extension $StructureDiffCopyWith on StructureDiff {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStructureDiff.copyWith(...)` or `instanceOfStructureDiff.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StructureDiffCWProxy get copyWith => _$StructureDiffCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StructureDiff _$StructureDiffFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('StructureDiff', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['format', 'addedPaths', 'removedPaths', 'typeChanged'],
  );
  final val = StructureDiff(
    format: $checkedConvert(
      'format',
      (v) => $enumDecode(_$StructureDiffFormatEnumEnumMap, v),
    ),
    fixtureFile: $checkedConvert('fixtureFile', (v) => v as String?),
    addedPaths: $checkedConvert(
      'addedPaths',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    removedPaths: $checkedConvert(
      'removedPaths',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    typeChanged: $checkedConvert(
      'typeChanged',
      (v) => (v as List<dynamic>)
          .map(
            (e) => StructureDiffTypeChangedInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    presenceDropped: $checkedConvert(
      'presenceDropped',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => StructureDiffPresenceDroppedInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    arrayEmpty: $checkedConvert(
      'arrayEmpty',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    columnsReordered: $checkedConvert('columnsReordered', (v) => v as bool?),
    delimiterChanged: $checkedConvert(
      'delimiterChanged',
      (v) => v == null
          ? null
          : StructureDiffDelimiterChanged.fromJson(v as Map<String, dynamic>),
    ),
    contentTypeChanged: $checkedConvert(
      'contentTypeChanged',
      (v) => v == null
          ? null
          : StructureDiffContentTypeChanged.fromJson(v as Map<String, dynamic>),
    ),
    sizeRatio: $checkedConvert('sizeRatio', (v) => v as num?),
    renameCandidates: $checkedConvert(
      'renameCandidates',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => StructureDiffRenameCandidatesInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    htmlTitle: $checkedConvert('htmlTitle', (v) => v as String?),
    examples: $checkedConvert(
      'examples',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) =>
            MapEntry(k, (e as List<dynamic>).map((e) => e as String).toList()),
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$StructureDiffToJson(
  StructureDiff instance,
) => <String, dynamic>{
  'format': _$StructureDiffFormatEnumEnumMap[instance.format]!,
  'fixtureFile': ?instance.fixtureFile,
  'addedPaths': instance.addedPaths,
  'removedPaths': instance.removedPaths,
  'typeChanged': instance.typeChanged.map((e) => e.toJson()).toList(),
  'presenceDropped': ?instance.presenceDropped?.map((e) => e.toJson()).toList(),
  'arrayEmpty': ?instance.arrayEmpty,
  'columnsReordered': ?instance.columnsReordered,
  'delimiterChanged': ?instance.delimiterChanged?.toJson(),
  'contentTypeChanged': ?instance.contentTypeChanged?.toJson(),
  'sizeRatio': ?instance.sizeRatio,
  'renameCandidates': ?instance.renameCandidates
      ?.map((e) => e.toJson())
      .toList(),
  'htmlTitle': ?instance.htmlTitle,
  'examples': ?instance.examples,
};

const _$StructureDiffFormatEnumEnumMap = {
  StructureDiffFormatEnum.json: 'json',
  StructureDiffFormatEnum.csv: 'csv',
  StructureDiffFormatEnum.tsv: 'tsv',
  StructureDiffFormatEnum.xml: 'xml',
  StructureDiffFormatEnum.binary: 'binary',
  StructureDiffFormatEnum.html: 'html',
  StructureDiffFormatEnum.text: 'text',
};
