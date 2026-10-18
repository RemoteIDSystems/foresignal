// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_export_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleExportRequestCWProxy {
  ChronicleExportRequest format(ChronicleExportRequestFormatEnum format);

  ChronicleExportRequest layers(List<String>? layers);

  ChronicleExportRequest from(DateTime? from);

  ChronicleExportRequest to(DateTime? to);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleExportRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleExportRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleExportRequest call({
    ChronicleExportRequestFormatEnum format,
    List<String>? layers,
    DateTime? from,
    DateTime? to,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleExportRequest.copyWith(...)` or call `instanceOfChronicleExportRequest.copyWith.fieldName(value)` for a single field.
class _$ChronicleExportRequestCWProxyImpl
    implements _$ChronicleExportRequestCWProxy {
  const _$ChronicleExportRequestCWProxyImpl(this._value);

  final ChronicleExportRequest _value;

  @override
  ChronicleExportRequest format(ChronicleExportRequestFormatEnum format) =>
      call(format: format);

  @override
  ChronicleExportRequest layers(List<String>? layers) => call(layers: layers);

  @override
  ChronicleExportRequest from(DateTime? from) => call(from: from);

  @override
  ChronicleExportRequest to(DateTime? to) => call(to: to);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleExportRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleExportRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleExportRequest call({
    Object? format = const $CopyWithPlaceholder(),
    Object? layers = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
  }) {
    return ChronicleExportRequest(
      format: format == const $CopyWithPlaceholder() || format == null
          ? _value.format
          // ignore: cast_nullable_to_non_nullable
          : format as ChronicleExportRequestFormatEnum,
      layers: layers == const $CopyWithPlaceholder()
          ? _value.layers
          // ignore: cast_nullable_to_non_nullable
          : layers as List<String>?,
      from: from == const $CopyWithPlaceholder()
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime?,
      to: to == const $CopyWithPlaceholder()
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime?,
    );
  }
}

extension $ChronicleExportRequestCopyWith on ChronicleExportRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleExportRequest.copyWith(...)` or `instanceOfChronicleExportRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleExportRequestCWProxy get copyWith =>
      _$ChronicleExportRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleExportRequest _$ChronicleExportRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleExportRequest', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['format']);
  final val = ChronicleExportRequest(
    format: $checkedConvert(
      'format',
      (v) => $enumDecode(_$ChronicleExportRequestFormatEnumEnumMap, v),
    ),
    layers: $checkedConvert(
      'layers',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    from: $checkedConvert(
      'from',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    to: $checkedConvert(
      'to',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$ChronicleExportRequestToJson(
  ChronicleExportRequest instance,
) => <String, dynamic>{
  'format': _$ChronicleExportRequestFormatEnumEnumMap[instance.format]!,
  'layers': ?instance.layers,
  'from': ?instance.from?.toIso8601String(),
  'to': ?instance.to?.toIso8601String(),
};

const _$ChronicleExportRequestFormatEnumEnumMap = {
  ChronicleExportRequestFormatEnum.geoparquet: 'geoparquet',
  ChronicleExportRequestFormatEnum.gpkg: 'gpkg',
  ChronicleExportRequestFormatEnum.jsonBundle: 'json-bundle',
};
