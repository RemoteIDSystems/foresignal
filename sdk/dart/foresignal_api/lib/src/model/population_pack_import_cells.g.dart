// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'population_pack_import_cells.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PopulationPackImportCellsCWProxy {
  PopulationPackImportCells res5(int? res5);

  PopulationPackImportCells res7(int? res7);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PopulationPackImportCells(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PopulationPackImportCells(...).copyWith(id: 12, name: "My name")
  /// ```
  PopulationPackImportCells call({int? res5, int? res7});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPopulationPackImportCells.copyWith(...)` or call `instanceOfPopulationPackImportCells.copyWith.fieldName(value)` for a single field.
class _$PopulationPackImportCellsCWProxyImpl
    implements _$PopulationPackImportCellsCWProxy {
  const _$PopulationPackImportCellsCWProxyImpl(this._value);

  final PopulationPackImportCells _value;

  @override
  PopulationPackImportCells res5(int? res5) => call(res5: res5);

  @override
  PopulationPackImportCells res7(int? res7) => call(res7: res7);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PopulationPackImportCells(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PopulationPackImportCells(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PopulationPackImportCells call({
    Object? res5 = const $CopyWithPlaceholder(),
    Object? res7 = const $CopyWithPlaceholder(),
  }) {
    return PopulationPackImportCells(
      res5: res5 == const $CopyWithPlaceholder()
          ? _value.res5
          // ignore: cast_nullable_to_non_nullable
          : res5 as int?,
      res7: res7 == const $CopyWithPlaceholder()
          ? _value.res7
          // ignore: cast_nullable_to_non_nullable
          : res7 as int?,
    );
  }
}

extension $PopulationPackImportCellsCopyWith on PopulationPackImportCells {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPopulationPackImportCells.copyWith(...)` or `instanceOfPopulationPackImportCells.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PopulationPackImportCellsCWProxy get copyWith =>
      _$PopulationPackImportCellsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PopulationPackImportCells _$PopulationPackImportCellsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PopulationPackImportCells', json, ($checkedConvert) {
  final val = PopulationPackImportCells(
    res5: $checkedConvert('res5', (v) => (v as num?)?.toInt()),
    res7: $checkedConvert('res7', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$PopulationPackImportCellsToJson(
  PopulationPackImportCells instance,
) => <String, dynamic>{'res5': ?instance.res5, 'res7': ?instance.res7};
