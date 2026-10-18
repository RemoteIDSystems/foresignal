// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'custom_source_formats_overland.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CustomSourceFormatsOverlandCWProxy {
  CustomSourceFormatsOverland keepWifi(bool? keepWifi);

  CustomSourceFormatsOverland maxAccuracyMeters(num? maxAccuracyMeters);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CustomSourceFormatsOverland(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CustomSourceFormatsOverland(...).copyWith(id: 12, name: "My name")
  /// ```
  CustomSourceFormatsOverland call({bool? keepWifi, num? maxAccuracyMeters});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCustomSourceFormatsOverland.copyWith(...)` or call `instanceOfCustomSourceFormatsOverland.copyWith.fieldName(value)` for a single field.
class _$CustomSourceFormatsOverlandCWProxyImpl
    implements _$CustomSourceFormatsOverlandCWProxy {
  const _$CustomSourceFormatsOverlandCWProxyImpl(this._value);

  final CustomSourceFormatsOverland _value;

  @override
  CustomSourceFormatsOverland keepWifi(bool? keepWifi) =>
      call(keepWifi: keepWifi);

  @override
  CustomSourceFormatsOverland maxAccuracyMeters(num? maxAccuracyMeters) =>
      call(maxAccuracyMeters: maxAccuracyMeters);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CustomSourceFormatsOverland(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CustomSourceFormatsOverland(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CustomSourceFormatsOverland call({
    Object? keepWifi = const $CopyWithPlaceholder(),
    Object? maxAccuracyMeters = const $CopyWithPlaceholder(),
  }) {
    return CustomSourceFormatsOverland(
      keepWifi: keepWifi == const $CopyWithPlaceholder()
          ? _value.keepWifi
          // ignore: cast_nullable_to_non_nullable
          : keepWifi as bool?,
      maxAccuracyMeters: maxAccuracyMeters == const $CopyWithPlaceholder()
          ? _value.maxAccuracyMeters
          // ignore: cast_nullable_to_non_nullable
          : maxAccuracyMeters as num?,
    );
  }
}

extension $CustomSourceFormatsOverlandCopyWith on CustomSourceFormatsOverland {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCustomSourceFormatsOverland.copyWith(...)` or `instanceOfCustomSourceFormatsOverland.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CustomSourceFormatsOverlandCWProxy get copyWith =>
      _$CustomSourceFormatsOverlandCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CustomSourceFormatsOverland _$CustomSourceFormatsOverlandFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CustomSourceFormatsOverland', json, ($checkedConvert) {
  final val = CustomSourceFormatsOverland(
    keepWifi: $checkedConvert('keepWifi', (v) => v as bool? ?? false),
    maxAccuracyMeters: $checkedConvert(
      'maxAccuracyMeters',
      (v) => v as num? ?? 200,
    ),
  );
  return val;
});

Map<String, dynamic> _$CustomSourceFormatsOverlandToJson(
  CustomSourceFormatsOverland instance,
) => <String, dynamic>{
  'keepWifi': ?instance.keepWifi,
  'maxAccuracyMeters': ?instance.maxAccuracyMeters,
};
