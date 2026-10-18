// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_block_one_of2.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportBlockOneOf2CWProxy {
  ReportBlockOneOf2 id(String? id);

  ReportBlockOneOf2 type(ReportBlockOneOf2TypeEnum type);

  ReportBlockOneOf2 title(String? title);

  ReportBlockOneOf2 textOrigin(ReportBlockOneOf2TextOriginEnum? textOrigin);

  ReportBlockOneOf2 updatedAt(DateTime? updatedAt);

  ReportBlockOneOf2 updatedBy(PrincipalSummary? updatedBy);

  ReportBlockOneOf2 items(List<ReportBlockOneOf2AllOfItemsInner> items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf2(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf2(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportBlockOneOf2 call({
    String? id,
    ReportBlockOneOf2TypeEnum type,
    String? title,
    ReportBlockOneOf2TextOriginEnum? textOrigin,
    DateTime? updatedAt,
    PrincipalSummary? updatedBy,
    List<ReportBlockOneOf2AllOfItemsInner> items,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportBlockOneOf2.copyWith(...)` or call `instanceOfReportBlockOneOf2.copyWith.fieldName(value)` for a single field.
class _$ReportBlockOneOf2CWProxyImpl implements _$ReportBlockOneOf2CWProxy {
  const _$ReportBlockOneOf2CWProxyImpl(this._value);

  final ReportBlockOneOf2 _value;

  @override
  ReportBlockOneOf2 id(String? id) => call(id: id);

  @override
  ReportBlockOneOf2 type(ReportBlockOneOf2TypeEnum type) => call(type: type);

  @override
  ReportBlockOneOf2 title(String? title) => call(title: title);

  @override
  ReportBlockOneOf2 textOrigin(ReportBlockOneOf2TextOriginEnum? textOrigin) =>
      call(textOrigin: textOrigin);

  @override
  ReportBlockOneOf2 updatedAt(DateTime? updatedAt) =>
      call(updatedAt: updatedAt);

  @override
  ReportBlockOneOf2 updatedBy(PrincipalSummary? updatedBy) =>
      call(updatedBy: updatedBy);

  @override
  ReportBlockOneOf2 items(List<ReportBlockOneOf2AllOfItemsInner> items) =>
      call(items: items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf2(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf2(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportBlockOneOf2 call({
    Object? id = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? textOrigin = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? updatedBy = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
  }) {
    return ReportBlockOneOf2(
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String?,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ReportBlockOneOf2TypeEnum,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      textOrigin: textOrigin == const $CopyWithPlaceholder()
          ? _value.textOrigin
          // ignore: cast_nullable_to_non_nullable
          : textOrigin as ReportBlockOneOf2TextOriginEnum?,
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
          : items as List<ReportBlockOneOf2AllOfItemsInner>,
    );
  }
}

extension $ReportBlockOneOf2CopyWith on ReportBlockOneOf2 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportBlockOneOf2.copyWith(...)` or `instanceOfReportBlockOneOf2.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportBlockOneOf2CWProxy get copyWith =>
      _$ReportBlockOneOf2CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportBlockOneOf2 _$ReportBlockOneOf2FromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReportBlockOneOf2', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type', 'items']);
      final val = ReportBlockOneOf2(
        id: $checkedConvert('id', (v) => v as String?),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$ReportBlockOneOf2TypeEnumEnumMap, v),
        ),
        title: $checkedConvert('title', (v) => v as String?),
        textOrigin: $checkedConvert(
          'textOrigin',
          (v) =>
              $enumDecodeNullable(_$ReportBlockOneOf2TextOriginEnumEnumMap, v),
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
                (e) => ReportBlockOneOf2AllOfItemsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ReportBlockOneOf2ToJson(
  ReportBlockOneOf2 instance,
) => <String, dynamic>{
  'id': ?instance.id,
  'type': _$ReportBlockOneOf2TypeEnumEnumMap[instance.type]!,
  'title': ?instance.title,
  'textOrigin': ?_$ReportBlockOneOf2TextOriginEnumEnumMap[instance.textOrigin],
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
  'updatedBy': ?instance.updatedBy?.toJson(),
  'items': instance.items.map((e) => e.toJson()).toList(),
};

const _$ReportBlockOneOf2TypeEnumEnumMap = {
  ReportBlockOneOf2TypeEnum.objects: 'objects',
};

const _$ReportBlockOneOf2TextOriginEnumEnumMap = {
  ReportBlockOneOf2TextOriginEnum.author: 'author',
  ReportBlockOneOf2TextOriginEnum.deterministic: 'deterministic',
  ReportBlockOneOf2TextOriginEnum.llm: 'llm',
};
