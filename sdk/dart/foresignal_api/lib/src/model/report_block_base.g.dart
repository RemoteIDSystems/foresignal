// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_block_base.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportBlockBaseCWProxy {
  ReportBlockBase id(String? id);

  ReportBlockBase type(ReportBlockBaseTypeEnum type);

  ReportBlockBase title(String? title);

  ReportBlockBase textOrigin(ReportBlockBaseTextOriginEnum? textOrigin);

  ReportBlockBase updatedAt(DateTime? updatedAt);

  ReportBlockBase updatedBy(PrincipalSummary? updatedBy);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockBase(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockBase(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportBlockBase call({
    String? id,
    ReportBlockBaseTypeEnum type,
    String? title,
    ReportBlockBaseTextOriginEnum? textOrigin,
    DateTime? updatedAt,
    PrincipalSummary? updatedBy,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportBlockBase.copyWith(...)` or call `instanceOfReportBlockBase.copyWith.fieldName(value)` for a single field.
class _$ReportBlockBaseCWProxyImpl implements _$ReportBlockBaseCWProxy {
  const _$ReportBlockBaseCWProxyImpl(this._value);

  final ReportBlockBase _value;

  @override
  ReportBlockBase id(String? id) => call(id: id);

  @override
  ReportBlockBase type(ReportBlockBaseTypeEnum type) => call(type: type);

  @override
  ReportBlockBase title(String? title) => call(title: title);

  @override
  ReportBlockBase textOrigin(ReportBlockBaseTextOriginEnum? textOrigin) =>
      call(textOrigin: textOrigin);

  @override
  ReportBlockBase updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  @override
  ReportBlockBase updatedBy(PrincipalSummary? updatedBy) =>
      call(updatedBy: updatedBy);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockBase(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockBase(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportBlockBase call({
    Object? id = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? textOrigin = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? updatedBy = const $CopyWithPlaceholder(),
  }) {
    return ReportBlockBase(
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String?,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ReportBlockBaseTypeEnum,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      textOrigin: textOrigin == const $CopyWithPlaceholder()
          ? _value.textOrigin
          // ignore: cast_nullable_to_non_nullable
          : textOrigin as ReportBlockBaseTextOriginEnum?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
      updatedBy: updatedBy == const $CopyWithPlaceholder()
          ? _value.updatedBy
          // ignore: cast_nullable_to_non_nullable
          : updatedBy as PrincipalSummary?,
    );
  }
}

extension $ReportBlockBaseCopyWith on ReportBlockBase {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportBlockBase.copyWith(...)` or `instanceOfReportBlockBase.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportBlockBaseCWProxy get copyWith => _$ReportBlockBaseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportBlockBase _$ReportBlockBaseFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReportBlockBase', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type']);
      final val = ReportBlockBase(
        id: $checkedConvert('id', (v) => v as String?),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$ReportBlockBaseTypeEnumEnumMap, v),
        ),
        title: $checkedConvert('title', (v) => v as String?),
        textOrigin: $checkedConvert(
          'textOrigin',
          (v) => $enumDecodeNullable(_$ReportBlockBaseTextOriginEnumEnumMap, v),
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
      );
      return val;
    });

Map<String, dynamic> _$ReportBlockBaseToJson(
  ReportBlockBase instance,
) => <String, dynamic>{
  'id': ?instance.id,
  'type': _$ReportBlockBaseTypeEnumEnumMap[instance.type]!,
  'title': ?instance.title,
  'textOrigin': ?_$ReportBlockBaseTextOriginEnumEnumMap[instance.textOrigin],
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
  'updatedBy': ?instance.updatedBy?.toJson(),
};

const _$ReportBlockBaseTypeEnumEnumMap = {
  ReportBlockBaseTypeEnum.markdown: 'markdown',
  ReportBlockBaseTypeEnum.bluf: 'bluf',
  ReportBlockBaseTypeEnum.objects: 'objects',
  ReportBlockBaseTypeEnum.map: 'map',
  ReportBlockBaseTypeEnum.timeline: 'timeline',
  ReportBlockBaseTypeEnum.hypotheses: 'hypotheses',
  ReportBlockBaseTypeEnum.forecasts: 'forecasts',
  ReportBlockBaseTypeEnum.evidence: 'evidence',
  ReportBlockBaseTypeEnum.coverage: 'coverage',
  ReportBlockBaseTypeEnum.attachments: 'attachments',
  ReportBlockBaseTypeEnum.sources: 'sources',
};

const _$ReportBlockBaseTextOriginEnumEnumMap = {
  ReportBlockBaseTextOriginEnum.author: 'author',
  ReportBlockBaseTextOriginEnum.deterministic: 'deterministic',
  ReportBlockBaseTextOriginEnum.llm: 'llm',
};
