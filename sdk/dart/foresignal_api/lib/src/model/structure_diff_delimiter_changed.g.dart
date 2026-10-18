// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'structure_diff_delimiter_changed.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StructureDiffDelimiterChangedCWProxy {
  StructureDiffDelimiterChanged from(String from);

  StructureDiffDelimiterChanged to(String to);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StructureDiffDelimiterChanged(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StructureDiffDelimiterChanged(...).copyWith(id: 12, name: "My name")
  /// ```
  StructureDiffDelimiterChanged call({String from, String to});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStructureDiffDelimiterChanged.copyWith(...)` or call `instanceOfStructureDiffDelimiterChanged.copyWith.fieldName(value)` for a single field.
class _$StructureDiffDelimiterChangedCWProxyImpl
    implements _$StructureDiffDelimiterChangedCWProxy {
  const _$StructureDiffDelimiterChangedCWProxyImpl(this._value);

  final StructureDiffDelimiterChanged _value;

  @override
  StructureDiffDelimiterChanged from(String from) => call(from: from);

  @override
  StructureDiffDelimiterChanged to(String to) => call(to: to);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StructureDiffDelimiterChanged(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StructureDiffDelimiterChanged(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StructureDiffDelimiterChanged call({
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
  }) {
    return StructureDiffDelimiterChanged(
      from: from == const $CopyWithPlaceholder() || from == null
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as String,
      to: to == const $CopyWithPlaceholder() || to == null
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as String,
    );
  }
}

extension $StructureDiffDelimiterChangedCopyWith
    on StructureDiffDelimiterChanged {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStructureDiffDelimiterChanged.copyWith(...)` or `instanceOfStructureDiffDelimiterChanged.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StructureDiffDelimiterChangedCWProxy get copyWith =>
      _$StructureDiffDelimiterChangedCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StructureDiffDelimiterChanged _$StructureDiffDelimiterChangedFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('StructureDiffDelimiterChanged', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['from', 'to']);
  final val = StructureDiffDelimiterChanged(
    from: $checkedConvert('from', (v) => v as String),
    to: $checkedConvert('to', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$StructureDiffDelimiterChangedToJson(
  StructureDiffDelimiterChanged instance,
) => <String, dynamic>{'from': instance.from, 'to': instance.to};
