// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_state_layers_inner_rollups_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleStateLayersInnerRollupsInnerCWProxy {
  ChronicleStateLayersInnerRollupsInner region(String region);

  ChronicleStateLayersInnerRollupsInner hour(DateTime hour);

  ChronicleStateLayersInnerRollupsInner count(int count);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleStateLayersInnerRollupsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleStateLayersInnerRollupsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleStateLayersInnerRollupsInner call({
    String region,
    DateTime hour,
    int count,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleStateLayersInnerRollupsInner.copyWith(...)` or call `instanceOfChronicleStateLayersInnerRollupsInner.copyWith.fieldName(value)` for a single field.
class _$ChronicleStateLayersInnerRollupsInnerCWProxyImpl
    implements _$ChronicleStateLayersInnerRollupsInnerCWProxy {
  const _$ChronicleStateLayersInnerRollupsInnerCWProxyImpl(this._value);

  final ChronicleStateLayersInnerRollupsInner _value;

  @override
  ChronicleStateLayersInnerRollupsInner region(String region) =>
      call(region: region);

  @override
  ChronicleStateLayersInnerRollupsInner hour(DateTime hour) => call(hour: hour);

  @override
  ChronicleStateLayersInnerRollupsInner count(int count) => call(count: count);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleStateLayersInnerRollupsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleStateLayersInnerRollupsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleStateLayersInnerRollupsInner call({
    Object? region = const $CopyWithPlaceholder(),
    Object? hour = const $CopyWithPlaceholder(),
    Object? count = const $CopyWithPlaceholder(),
  }) {
    return ChronicleStateLayersInnerRollupsInner(
      region: region == const $CopyWithPlaceholder() || region == null
          ? _value.region
          // ignore: cast_nullable_to_non_nullable
          : region as String,
      hour: hour == const $CopyWithPlaceholder() || hour == null
          ? _value.hour
          // ignore: cast_nullable_to_non_nullable
          : hour as DateTime,
      count: count == const $CopyWithPlaceholder() || count == null
          ? _value.count
          // ignore: cast_nullable_to_non_nullable
          : count as int,
    );
  }
}

extension $ChronicleStateLayersInnerRollupsInnerCopyWith
    on ChronicleStateLayersInnerRollupsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleStateLayersInnerRollupsInner.copyWith(...)` or `instanceOfChronicleStateLayersInnerRollupsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleStateLayersInnerRollupsInnerCWProxy get copyWith =>
      _$ChronicleStateLayersInnerRollupsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleStateLayersInnerRollupsInner
_$ChronicleStateLayersInnerRollupsInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ChronicleStateLayersInnerRollupsInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['region', 'hour', 'count']);
      final val = ChronicleStateLayersInnerRollupsInner(
        region: $checkedConvert('region', (v) => v as String),
        hour: $checkedConvert('hour', (v) => DateTime.parse(v as String)),
        count: $checkedConvert('count', (v) => (v as num).toInt()),
      );
      return val;
    });

Map<String, dynamic> _$ChronicleStateLayersInnerRollupsInnerToJson(
  ChronicleStateLayersInnerRollupsInner instance,
) => <String, dynamic>{
  'region': instance.region,
  'hour': instance.hour.toIso8601String(),
  'count': instance.count,
};
