// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_block_one_of8.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportBlockOneOf8CWProxy {
  ReportBlockOneOf8 id(String? id);

  ReportBlockOneOf8 type(ReportBlockOneOf8TypeEnum type);

  ReportBlockOneOf8 title(String? title);

  ReportBlockOneOf8 textOrigin(ReportBlockOneOf8TextOriginEnum? textOrigin);

  ReportBlockOneOf8 updatedAt(DateTime? updatedAt);

  ReportBlockOneOf8 updatedBy(PrincipalSummary? updatedBy);

  ReportBlockOneOf8 caveats(List<Caveat>? caveats);

  ReportBlockOneOf8 coverage(List<CoverageStats>? coverage);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf8(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf8(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportBlockOneOf8 call({
    String? id,
    ReportBlockOneOf8TypeEnum type,
    String? title,
    ReportBlockOneOf8TextOriginEnum? textOrigin,
    DateTime? updatedAt,
    PrincipalSummary? updatedBy,
    List<Caveat>? caveats,
    List<CoverageStats>? coverage,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportBlockOneOf8.copyWith(...)` or call `instanceOfReportBlockOneOf8.copyWith.fieldName(value)` for a single field.
class _$ReportBlockOneOf8CWProxyImpl implements _$ReportBlockOneOf8CWProxy {
  const _$ReportBlockOneOf8CWProxyImpl(this._value);

  final ReportBlockOneOf8 _value;

  @override
  ReportBlockOneOf8 id(String? id) => call(id: id);

  @override
  ReportBlockOneOf8 type(ReportBlockOneOf8TypeEnum type) => call(type: type);

  @override
  ReportBlockOneOf8 title(String? title) => call(title: title);

  @override
  ReportBlockOneOf8 textOrigin(ReportBlockOneOf8TextOriginEnum? textOrigin) =>
      call(textOrigin: textOrigin);

  @override
  ReportBlockOneOf8 updatedAt(DateTime? updatedAt) =>
      call(updatedAt: updatedAt);

  @override
  ReportBlockOneOf8 updatedBy(PrincipalSummary? updatedBy) =>
      call(updatedBy: updatedBy);

  @override
  ReportBlockOneOf8 caveats(List<Caveat>? caveats) => call(caveats: caveats);

  @override
  ReportBlockOneOf8 coverage(List<CoverageStats>? coverage) =>
      call(coverage: coverage);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf8(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf8(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportBlockOneOf8 call({
    Object? id = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? textOrigin = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? updatedBy = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
    Object? coverage = const $CopyWithPlaceholder(),
  }) {
    return ReportBlockOneOf8(
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String?,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ReportBlockOneOf8TypeEnum,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      textOrigin: textOrigin == const $CopyWithPlaceholder()
          ? _value.textOrigin
          // ignore: cast_nullable_to_non_nullable
          : textOrigin as ReportBlockOneOf8TextOriginEnum?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
      updatedBy: updatedBy == const $CopyWithPlaceholder()
          ? _value.updatedBy
          // ignore: cast_nullable_to_non_nullable
          : updatedBy as PrincipalSummary?,
      caveats: caveats == const $CopyWithPlaceholder()
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<Caveat>?,
      coverage: coverage == const $CopyWithPlaceholder()
          ? _value.coverage
          // ignore: cast_nullable_to_non_nullable
          : coverage as List<CoverageStats>?,
    );
  }
}

extension $ReportBlockOneOf8CopyWith on ReportBlockOneOf8 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportBlockOneOf8.copyWith(...)` or `instanceOfReportBlockOneOf8.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportBlockOneOf8CWProxy get copyWith =>
      _$ReportBlockOneOf8CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportBlockOneOf8 _$ReportBlockOneOf8FromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReportBlockOneOf8', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type']);
      final val = ReportBlockOneOf8(
        id: $checkedConvert('id', (v) => v as String?),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$ReportBlockOneOf8TypeEnumEnumMap, v),
        ),
        title: $checkedConvert('title', (v) => v as String?),
        textOrigin: $checkedConvert(
          'textOrigin',
          (v) =>
              $enumDecodeNullable(_$ReportBlockOneOf8TextOriginEnumEnumMap, v),
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
        caveats: $checkedConvert(
          'caveats',
          (v) => (v as List<dynamic>?)
              ?.map((e) => Caveat.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        coverage: $checkedConvert(
          'coverage',
          (v) => (v as List<dynamic>?)
              ?.map((e) => CoverageStats.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ReportBlockOneOf8ToJson(
  ReportBlockOneOf8 instance,
) => <String, dynamic>{
  'id': ?instance.id,
  'type': _$ReportBlockOneOf8TypeEnumEnumMap[instance.type]!,
  'title': ?instance.title,
  'textOrigin': ?_$ReportBlockOneOf8TextOriginEnumEnumMap[instance.textOrigin],
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
  'updatedBy': ?instance.updatedBy?.toJson(),
  'caveats': ?instance.caveats?.map((e) => e.toJson()).toList(),
  'coverage': ?instance.coverage?.map((e) => e.toJson()).toList(),
};

const _$ReportBlockOneOf8TypeEnumEnumMap = {
  ReportBlockOneOf8TypeEnum.coverage: 'coverage',
};

const _$ReportBlockOneOf8TextOriginEnumEnumMap = {
  ReportBlockOneOf8TextOriginEnum.author: 'author',
  ReportBlockOneOf8TextOriginEnum.deterministic: 'deterministic',
  ReportBlockOneOf8TextOriginEnum.llm: 'llm',
};
