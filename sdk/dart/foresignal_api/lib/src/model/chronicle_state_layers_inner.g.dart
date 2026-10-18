// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_state_layers_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleStateLayersInnerCWProxy {
  ChronicleStateLayersInner layerId(String layerId);

  ChronicleStateLayersInner name(String? name);

  ChronicleStateLayersInner capture(CaptureMode? capture);

  ChronicleStateLayersInner count(int count);

  ChronicleStateLayersInner items(List<ChronicleRecordVersion> items);

  ChronicleStateLayersInner rollups(
    List<ChronicleStateLayersInnerRollupsInner>? rollups,
  );

  ChronicleStateLayersInner nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleStateLayersInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleStateLayersInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleStateLayersInner call({
    String layerId,
    String? name,
    CaptureMode? capture,
    int count,
    List<ChronicleRecordVersion> items,
    List<ChronicleStateLayersInnerRollupsInner>? rollups,
    String? nextCursor,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleStateLayersInner.copyWith(...)` or call `instanceOfChronicleStateLayersInner.copyWith.fieldName(value)` for a single field.
class _$ChronicleStateLayersInnerCWProxyImpl
    implements _$ChronicleStateLayersInnerCWProxy {
  const _$ChronicleStateLayersInnerCWProxyImpl(this._value);

  final ChronicleStateLayersInner _value;

  @override
  ChronicleStateLayersInner layerId(String layerId) => call(layerId: layerId);

  @override
  ChronicleStateLayersInner name(String? name) => call(name: name);

  @override
  ChronicleStateLayersInner capture(CaptureMode? capture) =>
      call(capture: capture);

  @override
  ChronicleStateLayersInner count(int count) => call(count: count);

  @override
  ChronicleStateLayersInner items(List<ChronicleRecordVersion> items) =>
      call(items: items);

  @override
  ChronicleStateLayersInner rollups(
    List<ChronicleStateLayersInnerRollupsInner>? rollups,
  ) => call(rollups: rollups);

  @override
  ChronicleStateLayersInner nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleStateLayersInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleStateLayersInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleStateLayersInner call({
    Object? layerId = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? capture = const $CopyWithPlaceholder(),
    Object? count = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
    Object? rollups = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return ChronicleStateLayersInner(
      layerId: layerId == const $CopyWithPlaceholder() || layerId == null
          ? _value.layerId
          // ignore: cast_nullable_to_non_nullable
          : layerId as String,
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      capture: capture == const $CopyWithPlaceholder()
          ? _value.capture
          // ignore: cast_nullable_to_non_nullable
          : capture as CaptureMode?,
      count: count == const $CopyWithPlaceholder() || count == null
          ? _value.count
          // ignore: cast_nullable_to_non_nullable
          : count as int,
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<ChronicleRecordVersion>,
      rollups: rollups == const $CopyWithPlaceholder()
          ? _value.rollups
          // ignore: cast_nullable_to_non_nullable
          : rollups as List<ChronicleStateLayersInnerRollupsInner>?,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $ChronicleStateLayersInnerCopyWith on ChronicleStateLayersInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleStateLayersInner.copyWith(...)` or `instanceOfChronicleStateLayersInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleStateLayersInnerCWProxy get copyWith =>
      _$ChronicleStateLayersInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleStateLayersInner _$ChronicleStateLayersInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleStateLayersInner', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['layerId', 'count', 'items', 'nextCursor'],
  );
  final val = ChronicleStateLayersInner(
    layerId: $checkedConvert('layerId', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String?),
    capture: $checkedConvert(
      'capture',
      (v) => v == null ? null : CaptureMode.fromJson(v as Map<String, dynamic>),
    ),
    count: $checkedConvert('count', (v) => (v as num).toInt()),
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map(
            (e) => ChronicleRecordVersion.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    rollups: $checkedConvert(
      'rollups',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => ChronicleStateLayersInnerRollupsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ChronicleStateLayersInnerToJson(
  ChronicleStateLayersInner instance,
) => <String, dynamic>{
  'layerId': instance.layerId,
  'name': ?instance.name,
  'capture': ?instance.capture?.toJson(),
  'count': instance.count,
  'items': instance.items.map((e) => e.toJson()).toList(),
  'rollups': ?instance.rollups?.map((e) => e.toJson()).toList(),
  'nextCursor': instance.nextCursor,
};
