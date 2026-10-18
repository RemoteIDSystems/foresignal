// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system_info_raster_formats.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SystemInfoRasterFormatsCWProxy {
  SystemInfoRasterFormats webp(SystemInfoRasterFormatsWebp? webp);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoRasterFormats(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoRasterFormats(...).copyWith(id: 12, name: "My name")
  /// ```
  SystemInfoRasterFormats call({SystemInfoRasterFormatsWebp? webp});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSystemInfoRasterFormats.copyWith(...)` or call `instanceOfSystemInfoRasterFormats.copyWith.fieldName(value)` for a single field.
class _$SystemInfoRasterFormatsCWProxyImpl
    implements _$SystemInfoRasterFormatsCWProxy {
  const _$SystemInfoRasterFormatsCWProxyImpl(this._value);

  final SystemInfoRasterFormats _value;

  @override
  SystemInfoRasterFormats webp(SystemInfoRasterFormatsWebp? webp) =>
      call(webp: webp);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoRasterFormats(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoRasterFormats(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SystemInfoRasterFormats call({Object? webp = const $CopyWithPlaceholder()}) {
    return SystemInfoRasterFormats(
      webp: webp == const $CopyWithPlaceholder()
          ? _value.webp
          // ignore: cast_nullable_to_non_nullable
          : webp as SystemInfoRasterFormatsWebp?,
    );
  }
}

extension $SystemInfoRasterFormatsCopyWith on SystemInfoRasterFormats {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSystemInfoRasterFormats.copyWith(...)` or `instanceOfSystemInfoRasterFormats.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SystemInfoRasterFormatsCWProxy get copyWith =>
      _$SystemInfoRasterFormatsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SystemInfoRasterFormats _$SystemInfoRasterFormatsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SystemInfoRasterFormats', json, ($checkedConvert) {
  final val = SystemInfoRasterFormats(
    webp: $checkedConvert(
      'webp',
      (v) => v == null
          ? null
          : SystemInfoRasterFormatsWebp.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$SystemInfoRasterFormatsToJson(
  SystemInfoRasterFormats instance,
) => <String, dynamic>{'webp': ?instance.webp?.toJson()};
