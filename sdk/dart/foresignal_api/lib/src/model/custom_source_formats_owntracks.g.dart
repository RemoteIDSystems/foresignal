// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'custom_source_formats_owntracks.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CustomSourceFormatsOwntracksCWProxy {
  CustomSourceFormatsOwntracks maxAccuracyMeters(num? maxAccuracyMeters);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CustomSourceFormatsOwntracks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CustomSourceFormatsOwntracks(...).copyWith(id: 12, name: "My name")
  /// ```
  CustomSourceFormatsOwntracks call({num? maxAccuracyMeters});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCustomSourceFormatsOwntracks.copyWith(...)` or call `instanceOfCustomSourceFormatsOwntracks.copyWith.fieldName(value)` for a single field.
class _$CustomSourceFormatsOwntracksCWProxyImpl
    implements _$CustomSourceFormatsOwntracksCWProxy {
  const _$CustomSourceFormatsOwntracksCWProxyImpl(this._value);

  final CustomSourceFormatsOwntracks _value;

  @override
  CustomSourceFormatsOwntracks maxAccuracyMeters(num? maxAccuracyMeters) =>
      call(maxAccuracyMeters: maxAccuracyMeters);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CustomSourceFormatsOwntracks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CustomSourceFormatsOwntracks(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CustomSourceFormatsOwntracks call({
    Object? maxAccuracyMeters = const $CopyWithPlaceholder(),
  }) {
    return CustomSourceFormatsOwntracks(
      maxAccuracyMeters: maxAccuracyMeters == const $CopyWithPlaceholder()
          ? _value.maxAccuracyMeters
          // ignore: cast_nullable_to_non_nullable
          : maxAccuracyMeters as num?,
    );
  }
}

extension $CustomSourceFormatsOwntracksCopyWith
    on CustomSourceFormatsOwntracks {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCustomSourceFormatsOwntracks.copyWith(...)` or `instanceOfCustomSourceFormatsOwntracks.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CustomSourceFormatsOwntracksCWProxy get copyWith =>
      _$CustomSourceFormatsOwntracksCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CustomSourceFormatsOwntracks _$CustomSourceFormatsOwntracksFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CustomSourceFormatsOwntracks', json, ($checkedConvert) {
  final val = CustomSourceFormatsOwntracks(
    maxAccuracyMeters: $checkedConvert(
      'maxAccuracyMeters',
      (v) => v as num? ?? 200,
    ),
  );
  return val;
});

Map<String, dynamic> _$CustomSourceFormatsOwntracksToJson(
  CustomSourceFormatsOwntracks instance,
) => <String, dynamic>{'maxAccuracyMeters': ?instance.maxAccuracyMeters};
