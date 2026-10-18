// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_block_one_of9.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportBlockOneOf9CWProxy {
  ReportBlockOneOf9 id(String? id);

  ReportBlockOneOf9 type(ReportBlockOneOf9TypeEnum type);

  ReportBlockOneOf9 title(String? title);

  ReportBlockOneOf9 textOrigin(ReportBlockOneOf9TextOriginEnum? textOrigin);

  ReportBlockOneOf9 updatedAt(DateTime? updatedAt);

  ReportBlockOneOf9 updatedBy(PrincipalSummary? updatedBy);

  ReportBlockOneOf9 attachmentIds(List<String> attachmentIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf9(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf9(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportBlockOneOf9 call({
    String? id,
    ReportBlockOneOf9TypeEnum type,
    String? title,
    ReportBlockOneOf9TextOriginEnum? textOrigin,
    DateTime? updatedAt,
    PrincipalSummary? updatedBy,
    List<String> attachmentIds,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportBlockOneOf9.copyWith(...)` or call `instanceOfReportBlockOneOf9.copyWith.fieldName(value)` for a single field.
class _$ReportBlockOneOf9CWProxyImpl implements _$ReportBlockOneOf9CWProxy {
  const _$ReportBlockOneOf9CWProxyImpl(this._value);

  final ReportBlockOneOf9 _value;

  @override
  ReportBlockOneOf9 id(String? id) => call(id: id);

  @override
  ReportBlockOneOf9 type(ReportBlockOneOf9TypeEnum type) => call(type: type);

  @override
  ReportBlockOneOf9 title(String? title) => call(title: title);

  @override
  ReportBlockOneOf9 textOrigin(ReportBlockOneOf9TextOriginEnum? textOrigin) =>
      call(textOrigin: textOrigin);

  @override
  ReportBlockOneOf9 updatedAt(DateTime? updatedAt) =>
      call(updatedAt: updatedAt);

  @override
  ReportBlockOneOf9 updatedBy(PrincipalSummary? updatedBy) =>
      call(updatedBy: updatedBy);

  @override
  ReportBlockOneOf9 attachmentIds(List<String> attachmentIds) =>
      call(attachmentIds: attachmentIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf9(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf9(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportBlockOneOf9 call({
    Object? id = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? textOrigin = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? updatedBy = const $CopyWithPlaceholder(),
    Object? attachmentIds = const $CopyWithPlaceholder(),
  }) {
    return ReportBlockOneOf9(
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String?,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ReportBlockOneOf9TypeEnum,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      textOrigin: textOrigin == const $CopyWithPlaceholder()
          ? _value.textOrigin
          // ignore: cast_nullable_to_non_nullable
          : textOrigin as ReportBlockOneOf9TextOriginEnum?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
      updatedBy: updatedBy == const $CopyWithPlaceholder()
          ? _value.updatedBy
          // ignore: cast_nullable_to_non_nullable
          : updatedBy as PrincipalSummary?,
      attachmentIds:
          attachmentIds == const $CopyWithPlaceholder() || attachmentIds == null
          ? _value.attachmentIds
          // ignore: cast_nullable_to_non_nullable
          : attachmentIds as List<String>,
    );
  }
}

extension $ReportBlockOneOf9CopyWith on ReportBlockOneOf9 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportBlockOneOf9.copyWith(...)` or `instanceOfReportBlockOneOf9.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportBlockOneOf9CWProxy get copyWith =>
      _$ReportBlockOneOf9CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportBlockOneOf9 _$ReportBlockOneOf9FromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReportBlockOneOf9', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type', 'attachmentIds']);
      final val = ReportBlockOneOf9(
        id: $checkedConvert('id', (v) => v as String?),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$ReportBlockOneOf9TypeEnumEnumMap, v),
        ),
        title: $checkedConvert('title', (v) => v as String?),
        textOrigin: $checkedConvert(
          'textOrigin',
          (v) =>
              $enumDecodeNullable(_$ReportBlockOneOf9TextOriginEnumEnumMap, v),
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
        attachmentIds: $checkedConvert(
          'attachmentIds',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ReportBlockOneOf9ToJson(
  ReportBlockOneOf9 instance,
) => <String, dynamic>{
  'id': ?instance.id,
  'type': _$ReportBlockOneOf9TypeEnumEnumMap[instance.type]!,
  'title': ?instance.title,
  'textOrigin': ?_$ReportBlockOneOf9TextOriginEnumEnumMap[instance.textOrigin],
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
  'updatedBy': ?instance.updatedBy?.toJson(),
  'attachmentIds': instance.attachmentIds,
};

const _$ReportBlockOneOf9TypeEnumEnumMap = {
  ReportBlockOneOf9TypeEnum.attachments: 'attachments',
};

const _$ReportBlockOneOf9TextOriginEnumEnumMap = {
  ReportBlockOneOf9TextOriginEnum.author: 'author',
  ReportBlockOneOf9TextOriginEnum.deterministic: 'deterministic',
  ReportBlockOneOf9TextOriginEnum.llm: 'llm',
};
