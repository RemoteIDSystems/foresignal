// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'metar_payload_sky_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MetarPayloadSkyInnerCWProxy {
  MetarPayloadSkyInner cover(String cover);

  MetarPayloadSkyInner baseFtAgl(int? baseFtAgl);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MetarPayloadSkyInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MetarPayloadSkyInner(...).copyWith(id: 12, name: "My name")
  /// ```
  MetarPayloadSkyInner call({String cover, int? baseFtAgl});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMetarPayloadSkyInner.copyWith(...)` or call `instanceOfMetarPayloadSkyInner.copyWith.fieldName(value)` for a single field.
class _$MetarPayloadSkyInnerCWProxyImpl
    implements _$MetarPayloadSkyInnerCWProxy {
  const _$MetarPayloadSkyInnerCWProxyImpl(this._value);

  final MetarPayloadSkyInner _value;

  @override
  MetarPayloadSkyInner cover(String cover) => call(cover: cover);

  @override
  MetarPayloadSkyInner baseFtAgl(int? baseFtAgl) => call(baseFtAgl: baseFtAgl);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MetarPayloadSkyInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MetarPayloadSkyInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MetarPayloadSkyInner call({
    Object? cover = const $CopyWithPlaceholder(),
    Object? baseFtAgl = const $CopyWithPlaceholder(),
  }) {
    return MetarPayloadSkyInner(
      cover: cover == const $CopyWithPlaceholder() || cover == null
          ? _value.cover
          // ignore: cast_nullable_to_non_nullable
          : cover as String,
      baseFtAgl: baseFtAgl == const $CopyWithPlaceholder()
          ? _value.baseFtAgl
          // ignore: cast_nullable_to_non_nullable
          : baseFtAgl as int?,
    );
  }
}

extension $MetarPayloadSkyInnerCopyWith on MetarPayloadSkyInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMetarPayloadSkyInner.copyWith(...)` or `instanceOfMetarPayloadSkyInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MetarPayloadSkyInnerCWProxy get copyWith =>
      _$MetarPayloadSkyInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MetarPayloadSkyInner _$MetarPayloadSkyInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('MetarPayloadSkyInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['cover']);
  final val = MetarPayloadSkyInner(
    cover: $checkedConvert('cover', (v) => v as String),
    baseFtAgl: $checkedConvert('baseFtAgl', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$MetarPayloadSkyInnerToJson(
  MetarPayloadSkyInner instance,
) => <String, dynamic>{
  'cover': instance.cover,
  'baseFtAgl': ?instance.baseFtAgl,
};
