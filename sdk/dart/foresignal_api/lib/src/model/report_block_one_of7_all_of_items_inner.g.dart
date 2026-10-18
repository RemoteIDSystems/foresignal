// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_block_one_of7_all_of_items_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportBlockOneOf7AllOfItemsInnerCWProxy {
  ReportBlockOneOf7AllOfItemsInner evidenceItemId(String evidenceItemId);

  ReportBlockOneOf7AllOfItemsInner title(String? title);

  ReportBlockOneOf7AllOfItemsInner grade(String grade);

  ReportBlockOneOf7AllOfItemsInner snapshot(Map<String, Object>? snapshot);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf7AllOfItemsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf7AllOfItemsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportBlockOneOf7AllOfItemsInner call({
    String evidenceItemId,
    String? title,
    String grade,
    Map<String, Object>? snapshot,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportBlockOneOf7AllOfItemsInner.copyWith(...)` or call `instanceOfReportBlockOneOf7AllOfItemsInner.copyWith.fieldName(value)` for a single field.
class _$ReportBlockOneOf7AllOfItemsInnerCWProxyImpl
    implements _$ReportBlockOneOf7AllOfItemsInnerCWProxy {
  const _$ReportBlockOneOf7AllOfItemsInnerCWProxyImpl(this._value);

  final ReportBlockOneOf7AllOfItemsInner _value;

  @override
  ReportBlockOneOf7AllOfItemsInner evidenceItemId(String evidenceItemId) =>
      call(evidenceItemId: evidenceItemId);

  @override
  ReportBlockOneOf7AllOfItemsInner title(String? title) => call(title: title);

  @override
  ReportBlockOneOf7AllOfItemsInner grade(String grade) => call(grade: grade);

  @override
  ReportBlockOneOf7AllOfItemsInner snapshot(Map<String, Object>? snapshot) =>
      call(snapshot: snapshot);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf7AllOfItemsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf7AllOfItemsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportBlockOneOf7AllOfItemsInner call({
    Object? evidenceItemId = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? grade = const $CopyWithPlaceholder(),
    Object? snapshot = const $CopyWithPlaceholder(),
  }) {
    return ReportBlockOneOf7AllOfItemsInner(
      evidenceItemId:
          evidenceItemId == const $CopyWithPlaceholder() ||
              evidenceItemId == null
          ? _value.evidenceItemId
          // ignore: cast_nullable_to_non_nullable
          : evidenceItemId as String,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      grade: grade == const $CopyWithPlaceholder() || grade == null
          ? _value.grade
          // ignore: cast_nullable_to_non_nullable
          : grade as String,
      snapshot: snapshot == const $CopyWithPlaceholder()
          ? _value.snapshot
          // ignore: cast_nullable_to_non_nullable
          : snapshot as Map<String, Object>?,
    );
  }
}

extension $ReportBlockOneOf7AllOfItemsInnerCopyWith
    on ReportBlockOneOf7AllOfItemsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportBlockOneOf7AllOfItemsInner.copyWith(...)` or `instanceOfReportBlockOneOf7AllOfItemsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportBlockOneOf7AllOfItemsInnerCWProxy get copyWith =>
      _$ReportBlockOneOf7AllOfItemsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportBlockOneOf7AllOfItemsInner _$ReportBlockOneOf7AllOfItemsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ReportBlockOneOf7AllOfItemsInner', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['evidenceItemId', 'grade']);
  final val = ReportBlockOneOf7AllOfItemsInner(
    evidenceItemId: $checkedConvert('evidenceItemId', (v) => v as String),
    title: $checkedConvert('title', (v) => v as String?),
    grade: $checkedConvert('grade', (v) => v as String),
    snapshot: $checkedConvert(
      'snapshot',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
  );
  return val;
});

Map<String, dynamic> _$ReportBlockOneOf7AllOfItemsInnerToJson(
  ReportBlockOneOf7AllOfItemsInner instance,
) => <String, dynamic>{
  'evidenceItemId': instance.evidenceItemId,
  'title': ?instance.title,
  'grade': instance.grade,
  'snapshot': ?instance.snapshot,
};
