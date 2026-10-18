// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_block_one_of7.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportBlockOneOf7CWProxy {
  ReportBlockOneOf7 id(String? id);

  ReportBlockOneOf7 type(ReportBlockOneOf7TypeEnum type);

  ReportBlockOneOf7 title(String? title);

  ReportBlockOneOf7 textOrigin(ReportBlockOneOf7TextOriginEnum? textOrigin);

  ReportBlockOneOf7 updatedAt(DateTime? updatedAt);

  ReportBlockOneOf7 updatedBy(PrincipalSummary? updatedBy);

  ReportBlockOneOf7 items(List<ReportBlockOneOf7AllOfItemsInner> items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf7(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf7(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportBlockOneOf7 call({
    String? id,
    ReportBlockOneOf7TypeEnum type,
    String? title,
    ReportBlockOneOf7TextOriginEnum? textOrigin,
    DateTime? updatedAt,
    PrincipalSummary? updatedBy,
    List<ReportBlockOneOf7AllOfItemsInner> items,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportBlockOneOf7.copyWith(...)` or call `instanceOfReportBlockOneOf7.copyWith.fieldName(value)` for a single field.
class _$ReportBlockOneOf7CWProxyImpl implements _$ReportBlockOneOf7CWProxy {
  const _$ReportBlockOneOf7CWProxyImpl(this._value);

  final ReportBlockOneOf7 _value;

  @override
  ReportBlockOneOf7 id(String? id) => call(id: id);

  @override
  ReportBlockOneOf7 type(ReportBlockOneOf7TypeEnum type) => call(type: type);

  @override
  ReportBlockOneOf7 title(String? title) => call(title: title);

  @override
  ReportBlockOneOf7 textOrigin(ReportBlockOneOf7TextOriginEnum? textOrigin) =>
      call(textOrigin: textOrigin);

  @override
  ReportBlockOneOf7 updatedAt(DateTime? updatedAt) =>
      call(updatedAt: updatedAt);

  @override
  ReportBlockOneOf7 updatedBy(PrincipalSummary? updatedBy) =>
      call(updatedBy: updatedBy);

  @override
  ReportBlockOneOf7 items(List<ReportBlockOneOf7AllOfItemsInner> items) =>
      call(items: items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf7(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf7(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportBlockOneOf7 call({
    Object? id = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? textOrigin = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? updatedBy = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
  }) {
    return ReportBlockOneOf7(
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String?,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ReportBlockOneOf7TypeEnum,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      textOrigin: textOrigin == const $CopyWithPlaceholder()
          ? _value.textOrigin
          // ignore: cast_nullable_to_non_nullable
          : textOrigin as ReportBlockOneOf7TextOriginEnum?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
      updatedBy: updatedBy == const $CopyWithPlaceholder()
          ? _value.updatedBy
          // ignore: cast_nullable_to_non_nullable
          : updatedBy as PrincipalSummary?,
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<ReportBlockOneOf7AllOfItemsInner>,
    );
  }
}

extension $ReportBlockOneOf7CopyWith on ReportBlockOneOf7 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportBlockOneOf7.copyWith(...)` or `instanceOfReportBlockOneOf7.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportBlockOneOf7CWProxy get copyWith =>
      _$ReportBlockOneOf7CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportBlockOneOf7 _$ReportBlockOneOf7FromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReportBlockOneOf7', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type', 'items']);
      final val = ReportBlockOneOf7(
        id: $checkedConvert('id', (v) => v as String?),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$ReportBlockOneOf7TypeEnumEnumMap, v),
        ),
        title: $checkedConvert('title', (v) => v as String?),
        textOrigin: $checkedConvert(
          'textOrigin',
          (v) =>
              $enumDecodeNullable(_$ReportBlockOneOf7TextOriginEnumEnumMap, v),
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
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map(
                (e) => ReportBlockOneOf7AllOfItemsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ReportBlockOneOf7ToJson(
  ReportBlockOneOf7 instance,
) => <String, dynamic>{
  'id': ?instance.id,
  'type': _$ReportBlockOneOf7TypeEnumEnumMap[instance.type]!,
  'title': ?instance.title,
  'textOrigin': ?_$ReportBlockOneOf7TextOriginEnumEnumMap[instance.textOrigin],
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
  'updatedBy': ?instance.updatedBy?.toJson(),
  'items': instance.items.map((e) => e.toJson()).toList(),
};

const _$ReportBlockOneOf7TypeEnumEnumMap = {
  ReportBlockOneOf7TypeEnum.evidence: 'evidence',
};

const _$ReportBlockOneOf7TextOriginEnumEnumMap = {
  ReportBlockOneOf7TextOriginEnum.author: 'author',
  ReportBlockOneOf7TextOriginEnum.deterministic: 'deterministic',
  ReportBlockOneOf7TextOriginEnum.llm: 'llm',
};
