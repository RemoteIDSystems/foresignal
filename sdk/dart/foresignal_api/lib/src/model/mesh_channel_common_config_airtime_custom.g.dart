// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mesh_channel_common_config_airtime_custom.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MeshChannelCommonConfigAirtimeCustomCWProxy {
  MeshChannelCommonConfigAirtimeCustom spreadingFactor(int? spreadingFactor);

  MeshChannelCommonConfigAirtimeCustom bandwidthKhz(num? bandwidthKhz);

  MeshChannelCommonConfigAirtimeCustom codingRate(int? codingRate);

  MeshChannelCommonConfigAirtimeCustom preambleSymbols(int? preambleSymbols);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MeshChannelCommonConfigAirtimeCustom(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MeshChannelCommonConfigAirtimeCustom(...).copyWith(id: 12, name: "My name")
  /// ```
  MeshChannelCommonConfigAirtimeCustom call({
    int? spreadingFactor,
    num? bandwidthKhz,
    int? codingRate,
    int? preambleSymbols,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMeshChannelCommonConfigAirtimeCustom.copyWith(...)` or call `instanceOfMeshChannelCommonConfigAirtimeCustom.copyWith.fieldName(value)` for a single field.
class _$MeshChannelCommonConfigAirtimeCustomCWProxyImpl
    implements _$MeshChannelCommonConfigAirtimeCustomCWProxy {
  const _$MeshChannelCommonConfigAirtimeCustomCWProxyImpl(this._value);

  final MeshChannelCommonConfigAirtimeCustom _value;

  @override
  MeshChannelCommonConfigAirtimeCustom spreadingFactor(int? spreadingFactor) =>
      call(spreadingFactor: spreadingFactor);

  @override
  MeshChannelCommonConfigAirtimeCustom bandwidthKhz(num? bandwidthKhz) =>
      call(bandwidthKhz: bandwidthKhz);

  @override
  MeshChannelCommonConfigAirtimeCustom codingRate(int? codingRate) =>
      call(codingRate: codingRate);

  @override
  MeshChannelCommonConfigAirtimeCustom preambleSymbols(int? preambleSymbols) =>
      call(preambleSymbols: preambleSymbols);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MeshChannelCommonConfigAirtimeCustom(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MeshChannelCommonConfigAirtimeCustom(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MeshChannelCommonConfigAirtimeCustom call({
    Object? spreadingFactor = const $CopyWithPlaceholder(),
    Object? bandwidthKhz = const $CopyWithPlaceholder(),
    Object? codingRate = const $CopyWithPlaceholder(),
    Object? preambleSymbols = const $CopyWithPlaceholder(),
  }) {
    return MeshChannelCommonConfigAirtimeCustom(
      spreadingFactor: spreadingFactor == const $CopyWithPlaceholder()
          ? _value.spreadingFactor
          // ignore: cast_nullable_to_non_nullable
          : spreadingFactor as int?,
      bandwidthKhz: bandwidthKhz == const $CopyWithPlaceholder()
          ? _value.bandwidthKhz
          // ignore: cast_nullable_to_non_nullable
          : bandwidthKhz as num?,
      codingRate: codingRate == const $CopyWithPlaceholder()
          ? _value.codingRate
          // ignore: cast_nullable_to_non_nullable
          : codingRate as int?,
      preambleSymbols: preambleSymbols == const $CopyWithPlaceholder()
          ? _value.preambleSymbols
          // ignore: cast_nullable_to_non_nullable
          : preambleSymbols as int?,
    );
  }
}

extension $MeshChannelCommonConfigAirtimeCustomCopyWith
    on MeshChannelCommonConfigAirtimeCustom {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMeshChannelCommonConfigAirtimeCustom.copyWith(...)` or `instanceOfMeshChannelCommonConfigAirtimeCustom.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MeshChannelCommonConfigAirtimeCustomCWProxy get copyWith =>
      _$MeshChannelCommonConfigAirtimeCustomCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MeshChannelCommonConfigAirtimeCustom
_$MeshChannelCommonConfigAirtimeCustomFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MeshChannelCommonConfigAirtimeCustom', json, (
      $checkedConvert,
    ) {
      final val = MeshChannelCommonConfigAirtimeCustom(
        spreadingFactor: $checkedConvert(
          'spreadingFactor',
          (v) => (v as num?)?.toInt(),
        ),
        bandwidthKhz: $checkedConvert('bandwidthKhz', (v) => v as num?),
        codingRate: $checkedConvert('codingRate', (v) => (v as num?)?.toInt()),
        preambleSymbols: $checkedConvert(
          'preambleSymbols',
          (v) => (v as num?)?.toInt(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$MeshChannelCommonConfigAirtimeCustomToJson(
  MeshChannelCommonConfigAirtimeCustom instance,
) => <String, dynamic>{
  'spreadingFactor': ?instance.spreadingFactor,
  'bandwidthKhz': ?instance.bandwidthKhz,
  'codingRate': ?instance.codingRate,
  'preambleSymbols': ?instance.preambleSymbols,
};
