// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_layer_stats.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleLayerStatsCWProxy {
  ChronicleLayerStats rows(int? rows);

  ChronicleLayerStats bytes(int? bytes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayerStats(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayerStats(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleLayerStats call({int? rows, int? bytes});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleLayerStats.copyWith(...)` or call `instanceOfChronicleLayerStats.copyWith.fieldName(value)` for a single field.
class _$ChronicleLayerStatsCWProxyImpl implements _$ChronicleLayerStatsCWProxy {
  const _$ChronicleLayerStatsCWProxyImpl(this._value);

  final ChronicleLayerStats _value;

  @override
  ChronicleLayerStats rows(int? rows) => call(rows: rows);

  @override
  ChronicleLayerStats bytes(int? bytes) => call(bytes: bytes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayerStats(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayerStats(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleLayerStats call({
    Object? rows = const $CopyWithPlaceholder(),
    Object? bytes = const $CopyWithPlaceholder(),
  }) {
    return ChronicleLayerStats(
      rows: rows == const $CopyWithPlaceholder()
          ? _value.rows
          // ignore: cast_nullable_to_non_nullable
          : rows as int?,
      bytes: bytes == const $CopyWithPlaceholder()
          ? _value.bytes
          // ignore: cast_nullable_to_non_nullable
          : bytes as int?,
    );
  }
}

extension $ChronicleLayerStatsCopyWith on ChronicleLayerStats {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleLayerStats.copyWith(...)` or `instanceOfChronicleLayerStats.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleLayerStatsCWProxy get copyWith =>
      _$ChronicleLayerStatsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleLayerStats _$ChronicleLayerStatsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ChronicleLayerStats', json, ($checkedConvert) {
      final val = ChronicleLayerStats(
        rows: $checkedConvert('rows', (v) => (v as num?)?.toInt()),
        bytes: $checkedConvert('bytes', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$ChronicleLayerStatsToJson(
  ChronicleLayerStats instance,
) => <String, dynamic>{'rows': ?instance.rows, 'bytes': ?instance.bytes};
