// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'custom_source_formats_osmand.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CustomSourceFormatsOsmandCWProxy {
  CustomSourceFormatsOsmand maxAccuracyMeters(num? maxAccuracyMeters);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CustomSourceFormatsOsmand(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CustomSourceFormatsOsmand(...).copyWith(id: 12, name: "My name")
  /// ```
  CustomSourceFormatsOsmand call({num? maxAccuracyMeters});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCustomSourceFormatsOsmand.copyWith(...)` or call `instanceOfCustomSourceFormatsOsmand.copyWith.fieldName(value)` for a single field.
class _$CustomSourceFormatsOsmandCWProxyImpl
    implements _$CustomSourceFormatsOsmandCWProxy {
  const _$CustomSourceFormatsOsmandCWProxyImpl(this._value);

  final CustomSourceFormatsOsmand _value;

  @override
  CustomSourceFormatsOsmand maxAccuracyMeters(num? maxAccuracyMeters) =>
      call(maxAccuracyMeters: maxAccuracyMeters);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CustomSourceFormatsOsmand(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CustomSourceFormatsOsmand(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CustomSourceFormatsOsmand call({
    Object? maxAccuracyMeters = const $CopyWithPlaceholder(),
  }) {
    return CustomSourceFormatsOsmand(
      maxAccuracyMeters: maxAccuracyMeters == const $CopyWithPlaceholder()
          ? _value.maxAccuracyMeters
          // ignore: cast_nullable_to_non_nullable
          : maxAccuracyMeters as num?,
    );
  }
}

extension $CustomSourceFormatsOsmandCopyWith on CustomSourceFormatsOsmand {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCustomSourceFormatsOsmand.copyWith(...)` or `instanceOfCustomSourceFormatsOsmand.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CustomSourceFormatsOsmandCWProxy get copyWith =>
      _$CustomSourceFormatsOsmandCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CustomSourceFormatsOsmand _$CustomSourceFormatsOsmandFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CustomSourceFormatsOsmand', json, ($checkedConvert) {
  final val = CustomSourceFormatsOsmand(
    maxAccuracyMeters: $checkedConvert(
      'maxAccuracyMeters',
      (v) => v as num? ?? 200,
    ),
  );
  return val;
});

Map<String, dynamic> _$CustomSourceFormatsOsmandToJson(
  CustomSourceFormatsOsmand instance,
) => <String, dynamic>{'maxAccuracyMeters': ?instance.maxAccuracyMeters};
