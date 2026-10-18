// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_summary_top_entities_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleSummaryTopEntitiesInnerCWProxy {
  ChronicleSummaryTopEntitiesInner ref(String ref);

  ChronicleSummaryTopEntitiesInner title(String? title);

  ChronicleSummaryTopEntitiesInner count(int count);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSummaryTopEntitiesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSummaryTopEntitiesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleSummaryTopEntitiesInner call({String ref, String? title, int count});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleSummaryTopEntitiesInner.copyWith(...)` or call `instanceOfChronicleSummaryTopEntitiesInner.copyWith.fieldName(value)` for a single field.
class _$ChronicleSummaryTopEntitiesInnerCWProxyImpl
    implements _$ChronicleSummaryTopEntitiesInnerCWProxy {
  const _$ChronicleSummaryTopEntitiesInnerCWProxyImpl(this._value);

  final ChronicleSummaryTopEntitiesInner _value;

  @override
  ChronicleSummaryTopEntitiesInner ref(String ref) => call(ref: ref);

  @override
  ChronicleSummaryTopEntitiesInner title(String? title) => call(title: title);

  @override
  ChronicleSummaryTopEntitiesInner count(int count) => call(count: count);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSummaryTopEntitiesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSummaryTopEntitiesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleSummaryTopEntitiesInner call({
    Object? ref = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? count = const $CopyWithPlaceholder(),
  }) {
    return ChronicleSummaryTopEntitiesInner(
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      count: count == const $CopyWithPlaceholder() || count == null
          ? _value.count
          // ignore: cast_nullable_to_non_nullable
          : count as int,
    );
  }
}

extension $ChronicleSummaryTopEntitiesInnerCopyWith
    on ChronicleSummaryTopEntitiesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleSummaryTopEntitiesInner.copyWith(...)` or `instanceOfChronicleSummaryTopEntitiesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleSummaryTopEntitiesInnerCWProxy get copyWith =>
      _$ChronicleSummaryTopEntitiesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleSummaryTopEntitiesInner _$ChronicleSummaryTopEntitiesInnerFromJson(
  Map<String, dynamic> json,
) =>
    $checkedCreate('ChronicleSummaryTopEntitiesInner', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['ref', 'count']);
      final val = ChronicleSummaryTopEntitiesInner(
        ref: $checkedConvert('ref', (v) => v as String),
        title: $checkedConvert('title', (v) => v as String?),
        count: $checkedConvert('count', (v) => (v as num).toInt()),
      );
      return val;
    });

Map<String, dynamic> _$ChronicleSummaryTopEntitiesInnerToJson(
  ChronicleSummaryTopEntitiesInner instance,
) => <String, dynamic>{
  'ref': instance.ref,
  'title': ?instance.title,
  'count': instance.count,
};
