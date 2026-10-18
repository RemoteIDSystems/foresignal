// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cycle_packet_outlooks.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CyclePacketOutlooksCWProxy {
  CyclePacketOutlooks indexChanges(List<CyclePacketItem>? indexChanges);

  CyclePacketOutlooks windowsOutsideP10P90(
    List<CyclePacketItem>? windowsOutsideP10P90,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CyclePacketOutlooks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CyclePacketOutlooks(...).copyWith(id: 12, name: "My name")
  /// ```
  CyclePacketOutlooks call({
    List<CyclePacketItem>? indexChanges,
    List<CyclePacketItem>? windowsOutsideP10P90,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCyclePacketOutlooks.copyWith(...)` or call `instanceOfCyclePacketOutlooks.copyWith.fieldName(value)` for a single field.
class _$CyclePacketOutlooksCWProxyImpl implements _$CyclePacketOutlooksCWProxy {
  const _$CyclePacketOutlooksCWProxyImpl(this._value);

  final CyclePacketOutlooks _value;

  @override
  CyclePacketOutlooks indexChanges(List<CyclePacketItem>? indexChanges) =>
      call(indexChanges: indexChanges);

  @override
  CyclePacketOutlooks windowsOutsideP10P90(
    List<CyclePacketItem>? windowsOutsideP10P90,
  ) => call(windowsOutsideP10P90: windowsOutsideP10P90);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CyclePacketOutlooks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CyclePacketOutlooks(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CyclePacketOutlooks call({
    Object? indexChanges = const $CopyWithPlaceholder(),
    Object? windowsOutsideP10P90 = const $CopyWithPlaceholder(),
  }) {
    return CyclePacketOutlooks(
      indexChanges: indexChanges == const $CopyWithPlaceholder()
          ? _value.indexChanges
          // ignore: cast_nullable_to_non_nullable
          : indexChanges as List<CyclePacketItem>?,
      windowsOutsideP10P90: windowsOutsideP10P90 == const $CopyWithPlaceholder()
          ? _value.windowsOutsideP10P90
          // ignore: cast_nullable_to_non_nullable
          : windowsOutsideP10P90 as List<CyclePacketItem>?,
    );
  }
}

extension $CyclePacketOutlooksCopyWith on CyclePacketOutlooks {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCyclePacketOutlooks.copyWith(...)` or `instanceOfCyclePacketOutlooks.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CyclePacketOutlooksCWProxy get copyWith =>
      _$CyclePacketOutlooksCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CyclePacketOutlooks _$CyclePacketOutlooksFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CyclePacketOutlooks', json, ($checkedConvert) {
      final val = CyclePacketOutlooks(
        indexChanges: $checkedConvert(
          'indexChanges',
          (v) => (v as List<dynamic>?)
              ?.map((e) => CyclePacketItem.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        windowsOutsideP10P90: $checkedConvert(
          'windowsOutsideP10P90',
          (v) => (v as List<dynamic>?)
              ?.map((e) => CyclePacketItem.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$CyclePacketOutlooksToJson(
  CyclePacketOutlooks instance,
) => <String, dynamic>{
  'indexChanges': ?instance.indexChanges?.map((e) => e.toJson()).toList(),
  'windowsOutsideP10P90': ?instance.windowsOutsideP10P90
      ?.map((e) => e.toJson())
      .toList(),
};
