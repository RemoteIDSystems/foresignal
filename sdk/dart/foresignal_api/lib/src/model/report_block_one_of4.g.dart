// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_block_one_of4.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportBlockOneOf4CWProxy {
  ReportBlockOneOf4 id(String? id);

  ReportBlockOneOf4 type(ReportBlockOneOf4TypeEnum type);

  ReportBlockOneOf4 title(String? title);

  ReportBlockOneOf4 textOrigin(ReportBlockOneOf4TextOriginEnum? textOrigin);

  ReportBlockOneOf4 updatedAt(DateTime? updatedAt);

  ReportBlockOneOf4 updatedBy(PrincipalSummary? updatedBy);

  ReportBlockOneOf4 sourceRef(String? sourceRef);

  ReportBlockOneOf4 items(List<ReportBlockOneOf4AllOfItemsInner> items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf4(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf4(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportBlockOneOf4 call({
    String? id,
    ReportBlockOneOf4TypeEnum type,
    String? title,
    ReportBlockOneOf4TextOriginEnum? textOrigin,
    DateTime? updatedAt,
    PrincipalSummary? updatedBy,
    String? sourceRef,
    List<ReportBlockOneOf4AllOfItemsInner> items,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportBlockOneOf4.copyWith(...)` or call `instanceOfReportBlockOneOf4.copyWith.fieldName(value)` for a single field.
class _$ReportBlockOneOf4CWProxyImpl implements _$ReportBlockOneOf4CWProxy {
  const _$ReportBlockOneOf4CWProxyImpl(this._value);

  final ReportBlockOneOf4 _value;

  @override
  ReportBlockOneOf4 id(String? id) => call(id: id);

  @override
  ReportBlockOneOf4 type(ReportBlockOneOf4TypeEnum type) => call(type: type);

  @override
  ReportBlockOneOf4 title(String? title) => call(title: title);

  @override
  ReportBlockOneOf4 textOrigin(ReportBlockOneOf4TextOriginEnum? textOrigin) =>
      call(textOrigin: textOrigin);

  @override
  ReportBlockOneOf4 updatedAt(DateTime? updatedAt) =>
      call(updatedAt: updatedAt);

  @override
  ReportBlockOneOf4 updatedBy(PrincipalSummary? updatedBy) =>
      call(updatedBy: updatedBy);

  @override
  ReportBlockOneOf4 sourceRef(String? sourceRef) => call(sourceRef: sourceRef);

  @override
  ReportBlockOneOf4 items(List<ReportBlockOneOf4AllOfItemsInner> items) =>
      call(items: items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf4(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf4(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportBlockOneOf4 call({
    Object? id = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? textOrigin = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? updatedBy = const $CopyWithPlaceholder(),
    Object? sourceRef = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
  }) {
    return ReportBlockOneOf4(
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String?,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ReportBlockOneOf4TypeEnum,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      textOrigin: textOrigin == const $CopyWithPlaceholder()
          ? _value.textOrigin
          // ignore: cast_nullable_to_non_nullable
          : textOrigin as ReportBlockOneOf4TextOriginEnum?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
      updatedBy: updatedBy == const $CopyWithPlaceholder()
          ? _value.updatedBy
          // ignore: cast_nullable_to_non_nullable
          : updatedBy as PrincipalSummary?,
      sourceRef: sourceRef == const $CopyWithPlaceholder()
          ? _value.sourceRef
          // ignore: cast_nullable_to_non_nullable
          : sourceRef as String?,
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<ReportBlockOneOf4AllOfItemsInner>,
    );
  }
}

extension $ReportBlockOneOf4CopyWith on ReportBlockOneOf4 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportBlockOneOf4.copyWith(...)` or `instanceOfReportBlockOneOf4.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportBlockOneOf4CWProxy get copyWith =>
      _$ReportBlockOneOf4CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportBlockOneOf4 _$ReportBlockOneOf4FromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReportBlockOneOf4', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type', 'items']);
      final val = ReportBlockOneOf4(
        id: $checkedConvert('id', (v) => v as String?),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$ReportBlockOneOf4TypeEnumEnumMap, v),
        ),
        title: $checkedConvert('title', (v) => v as String?),
        textOrigin: $checkedConvert(
          'textOrigin',
          (v) =>
              $enumDecodeNullable(_$ReportBlockOneOf4TextOriginEnumEnumMap, v),
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
        sourceRef: $checkedConvert('sourceRef', (v) => v as String?),
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map(
                (e) => ReportBlockOneOf4AllOfItemsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ReportBlockOneOf4ToJson(
  ReportBlockOneOf4 instance,
) => <String, dynamic>{
  'id': ?instance.id,
  'type': _$ReportBlockOneOf4TypeEnumEnumMap[instance.type]!,
  'title': ?instance.title,
  'textOrigin': ?_$ReportBlockOneOf4TextOriginEnumEnumMap[instance.textOrigin],
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
  'updatedBy': ?instance.updatedBy?.toJson(),
  'sourceRef': ?instance.sourceRef,
  'items': instance.items.map((e) => e.toJson()).toList(),
};

const _$ReportBlockOneOf4TypeEnumEnumMap = {
  ReportBlockOneOf4TypeEnum.timeline: 'timeline',
};

const _$ReportBlockOneOf4TextOriginEnumEnumMap = {
  ReportBlockOneOf4TextOriginEnum.author: 'author',
  ReportBlockOneOf4TextOriginEnum.deterministic: 'deterministic',
  ReportBlockOneOf4TextOriginEnum.llm: 'llm',
};
