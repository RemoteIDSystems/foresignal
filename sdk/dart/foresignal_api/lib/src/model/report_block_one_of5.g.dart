// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_block_one_of5.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportBlockOneOf5CWProxy {
  ReportBlockOneOf5 id(String? id);

  ReportBlockOneOf5 type(ReportBlockOneOf5TypeEnum type);

  ReportBlockOneOf5 title(String? title);

  ReportBlockOneOf5 textOrigin(ReportBlockOneOf5TextOriginEnum? textOrigin);

  ReportBlockOneOf5 updatedAt(DateTime? updatedAt);

  ReportBlockOneOf5 updatedBy(PrincipalSummary? updatedBy);

  ReportBlockOneOf5 hypothesisSetRef(String hypothesisSetRef);

  ReportBlockOneOf5 snapshot(Map<String, Object>? snapshot);

  ReportBlockOneOf5 revisionIds(List<String>? revisionIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf5(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf5(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportBlockOneOf5 call({
    String? id,
    ReportBlockOneOf5TypeEnum type,
    String? title,
    ReportBlockOneOf5TextOriginEnum? textOrigin,
    DateTime? updatedAt,
    PrincipalSummary? updatedBy,
    String hypothesisSetRef,
    Map<String, Object>? snapshot,
    List<String>? revisionIds,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportBlockOneOf5.copyWith(...)` or call `instanceOfReportBlockOneOf5.copyWith.fieldName(value)` for a single field.
class _$ReportBlockOneOf5CWProxyImpl implements _$ReportBlockOneOf5CWProxy {
  const _$ReportBlockOneOf5CWProxyImpl(this._value);

  final ReportBlockOneOf5 _value;

  @override
  ReportBlockOneOf5 id(String? id) => call(id: id);

  @override
  ReportBlockOneOf5 type(ReportBlockOneOf5TypeEnum type) => call(type: type);

  @override
  ReportBlockOneOf5 title(String? title) => call(title: title);

  @override
  ReportBlockOneOf5 textOrigin(ReportBlockOneOf5TextOriginEnum? textOrigin) =>
      call(textOrigin: textOrigin);

  @override
  ReportBlockOneOf5 updatedAt(DateTime? updatedAt) =>
      call(updatedAt: updatedAt);

  @override
  ReportBlockOneOf5 updatedBy(PrincipalSummary? updatedBy) =>
      call(updatedBy: updatedBy);

  @override
  ReportBlockOneOf5 hypothesisSetRef(String hypothesisSetRef) =>
      call(hypothesisSetRef: hypothesisSetRef);

  @override
  ReportBlockOneOf5 snapshot(Map<String, Object>? snapshot) =>
      call(snapshot: snapshot);

  @override
  ReportBlockOneOf5 revisionIds(List<String>? revisionIds) =>
      call(revisionIds: revisionIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf5(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf5(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportBlockOneOf5 call({
    Object? id = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? textOrigin = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? updatedBy = const $CopyWithPlaceholder(),
    Object? hypothesisSetRef = const $CopyWithPlaceholder(),
    Object? snapshot = const $CopyWithPlaceholder(),
    Object? revisionIds = const $CopyWithPlaceholder(),
  }) {
    return ReportBlockOneOf5(
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String?,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ReportBlockOneOf5TypeEnum,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      textOrigin: textOrigin == const $CopyWithPlaceholder()
          ? _value.textOrigin
          // ignore: cast_nullable_to_non_nullable
          : textOrigin as ReportBlockOneOf5TextOriginEnum?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
      updatedBy: updatedBy == const $CopyWithPlaceholder()
          ? _value.updatedBy
          // ignore: cast_nullable_to_non_nullable
          : updatedBy as PrincipalSummary?,
      hypothesisSetRef:
          hypothesisSetRef == const $CopyWithPlaceholder() ||
              hypothesisSetRef == null
          ? _value.hypothesisSetRef
          // ignore: cast_nullable_to_non_nullable
          : hypothesisSetRef as String,
      snapshot: snapshot == const $CopyWithPlaceholder()
          ? _value.snapshot
          // ignore: cast_nullable_to_non_nullable
          : snapshot as Map<String, Object>?,
      revisionIds: revisionIds == const $CopyWithPlaceholder()
          ? _value.revisionIds
          // ignore: cast_nullable_to_non_nullable
          : revisionIds as List<String>?,
    );
  }
}

extension $ReportBlockOneOf5CopyWith on ReportBlockOneOf5 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportBlockOneOf5.copyWith(...)` or `instanceOfReportBlockOneOf5.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportBlockOneOf5CWProxy get copyWith =>
      _$ReportBlockOneOf5CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportBlockOneOf5 _$ReportBlockOneOf5FromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ReportBlockOneOf5', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['type', 'hypothesisSetRef']);
  final val = ReportBlockOneOf5(
    id: $checkedConvert('id', (v) => v as String?),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$ReportBlockOneOf5TypeEnumEnumMap, v),
    ),
    title: $checkedConvert('title', (v) => v as String?),
    textOrigin: $checkedConvert(
      'textOrigin',
      (v) => $enumDecodeNullable(_$ReportBlockOneOf5TextOriginEnumEnumMap, v),
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
    hypothesisSetRef: $checkedConvert('hypothesisSetRef', (v) => v as String),
    snapshot: $checkedConvert(
      'snapshot',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    revisionIds: $checkedConvert(
      'revisionIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ReportBlockOneOf5ToJson(
  ReportBlockOneOf5 instance,
) => <String, dynamic>{
  'id': ?instance.id,
  'type': _$ReportBlockOneOf5TypeEnumEnumMap[instance.type]!,
  'title': ?instance.title,
  'textOrigin': ?_$ReportBlockOneOf5TextOriginEnumEnumMap[instance.textOrigin],
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
  'updatedBy': ?instance.updatedBy?.toJson(),
  'hypothesisSetRef': instance.hypothesisSetRef,
  'snapshot': ?instance.snapshot,
  'revisionIds': ?instance.revisionIds,
};

const _$ReportBlockOneOf5TypeEnumEnumMap = {
  ReportBlockOneOf5TypeEnum.hypotheses: 'hypotheses',
};

const _$ReportBlockOneOf5TextOriginEnumEnumMap = {
  ReportBlockOneOf5TextOriginEnum.author: 'author',
  ReportBlockOneOf5TextOriginEnum.deterministic: 'deterministic',
  ReportBlockOneOf5TextOriginEnum.llm: 'llm',
};
