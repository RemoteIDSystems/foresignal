// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_block_one_of.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportBlockOneOfCWProxy {
  ReportBlockOneOf id(String? id);

  ReportBlockOneOf type(ReportBlockOneOfTypeEnum type);

  ReportBlockOneOf title(String? title);

  ReportBlockOneOf textOrigin(ReportBlockOneOfTextOriginEnum? textOrigin);

  ReportBlockOneOf updatedAt(DateTime? updatedAt);

  ReportBlockOneOf updatedBy(PrincipalSummary? updatedBy);

  ReportBlockOneOf markdown(String markdown);

  ReportBlockOneOf citations(List<String>? citations);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportBlockOneOf call({
    String? id,
    ReportBlockOneOfTypeEnum type,
    String? title,
    ReportBlockOneOfTextOriginEnum? textOrigin,
    DateTime? updatedAt,
    PrincipalSummary? updatedBy,
    String markdown,
    List<String>? citations,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportBlockOneOf.copyWith(...)` or call `instanceOfReportBlockOneOf.copyWith.fieldName(value)` for a single field.
class _$ReportBlockOneOfCWProxyImpl implements _$ReportBlockOneOfCWProxy {
  const _$ReportBlockOneOfCWProxyImpl(this._value);

  final ReportBlockOneOf _value;

  @override
  ReportBlockOneOf id(String? id) => call(id: id);

  @override
  ReportBlockOneOf type(ReportBlockOneOfTypeEnum type) => call(type: type);

  @override
  ReportBlockOneOf title(String? title) => call(title: title);

  @override
  ReportBlockOneOf textOrigin(ReportBlockOneOfTextOriginEnum? textOrigin) =>
      call(textOrigin: textOrigin);

  @override
  ReportBlockOneOf updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  @override
  ReportBlockOneOf updatedBy(PrincipalSummary? updatedBy) =>
      call(updatedBy: updatedBy);

  @override
  ReportBlockOneOf markdown(String markdown) => call(markdown: markdown);

  @override
  ReportBlockOneOf citations(List<String>? citations) =>
      call(citations: citations);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportBlockOneOf call({
    Object? id = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? textOrigin = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? updatedBy = const $CopyWithPlaceholder(),
    Object? markdown = const $CopyWithPlaceholder(),
    Object? citations = const $CopyWithPlaceholder(),
  }) {
    return ReportBlockOneOf(
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String?,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ReportBlockOneOfTypeEnum,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      textOrigin: textOrigin == const $CopyWithPlaceholder()
          ? _value.textOrigin
          // ignore: cast_nullable_to_non_nullable
          : textOrigin as ReportBlockOneOfTextOriginEnum?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
      updatedBy: updatedBy == const $CopyWithPlaceholder()
          ? _value.updatedBy
          // ignore: cast_nullable_to_non_nullable
          : updatedBy as PrincipalSummary?,
      markdown: markdown == const $CopyWithPlaceholder() || markdown == null
          ? _value.markdown
          // ignore: cast_nullable_to_non_nullable
          : markdown as String,
      citations: citations == const $CopyWithPlaceholder()
          ? _value.citations
          // ignore: cast_nullable_to_non_nullable
          : citations as List<String>?,
    );
  }
}

extension $ReportBlockOneOfCopyWith on ReportBlockOneOf {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportBlockOneOf.copyWith(...)` or `instanceOfReportBlockOneOf.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportBlockOneOfCWProxy get copyWith => _$ReportBlockOneOfCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportBlockOneOf _$ReportBlockOneOfFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReportBlockOneOf', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type', 'markdown']);
      final val = ReportBlockOneOf(
        id: $checkedConvert('id', (v) => v as String?),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$ReportBlockOneOfTypeEnumEnumMap, v),
        ),
        title: $checkedConvert('title', (v) => v as String?),
        textOrigin: $checkedConvert(
          'textOrigin',
          (v) =>
              $enumDecodeNullable(_$ReportBlockOneOfTextOriginEnumEnumMap, v),
        ),
        updatedAt: $checkedConvert(
          'updatedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        updatedBy: $checkedConvert(
          'updatedBy',
          (v) => v == null
              ? null
              : PrincipalSummary.fromJson(v as Map<String, dynamic>),
        ),
        markdown: $checkedConvert('markdown', (v) => v as String),
        citations: $checkedConvert(
          'citations',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ReportBlockOneOfToJson(
  ReportBlockOneOf instance,
) => <String, dynamic>{
  'id': ?instance.id,
  'type': _$ReportBlockOneOfTypeEnumEnumMap[instance.type]!,
  'title': ?instance.title,
  'textOrigin': ?_$ReportBlockOneOfTextOriginEnumEnumMap[instance.textOrigin],
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
  'updatedBy': ?instance.updatedBy?.toJson(),
  'markdown': instance.markdown,
  'citations': ?instance.citations,
};

const _$ReportBlockOneOfTypeEnumEnumMap = {
  ReportBlockOneOfTypeEnum.markdown: 'markdown',
};

const _$ReportBlockOneOfTextOriginEnumEnumMap = {
  ReportBlockOneOfTextOriginEnum.author: 'author',
  ReportBlockOneOfTextOriginEnum.deterministic: 'deterministic',
  ReportBlockOneOfTextOriginEnum.llm: 'llm',
};
