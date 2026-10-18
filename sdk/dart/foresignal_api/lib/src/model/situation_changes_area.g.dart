// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_changes_area.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationChangesAreaCWProxy {
  SituationChangesArea beforeKm2(num? beforeKm2);

  SituationChangesArea afterKm2(num? afterKm2);

  SituationChangesArea deltaKm2(num? deltaKm2);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationChangesArea(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationChangesArea(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationChangesArea call({num? beforeKm2, num? afterKm2, num? deltaKm2});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationChangesArea.copyWith(...)` or call `instanceOfSituationChangesArea.copyWith.fieldName(value)` for a single field.
class _$SituationChangesAreaCWProxyImpl
    implements _$SituationChangesAreaCWProxy {
  const _$SituationChangesAreaCWProxyImpl(this._value);

  final SituationChangesArea _value;

  @override
  SituationChangesArea beforeKm2(num? beforeKm2) => call(beforeKm2: beforeKm2);

  @override
  SituationChangesArea afterKm2(num? afterKm2) => call(afterKm2: afterKm2);

  @override
  SituationChangesArea deltaKm2(num? deltaKm2) => call(deltaKm2: deltaKm2);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationChangesArea(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationChangesArea(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationChangesArea call({
    Object? beforeKm2 = const $CopyWithPlaceholder(),
    Object? afterKm2 = const $CopyWithPlaceholder(),
    Object? deltaKm2 = const $CopyWithPlaceholder(),
  }) {
    return SituationChangesArea(
      beforeKm2: beforeKm2 == const $CopyWithPlaceholder()
          ? _value.beforeKm2
          // ignore: cast_nullable_to_non_nullable
          : beforeKm2 as num?,
      afterKm2: afterKm2 == const $CopyWithPlaceholder()
          ? _value.afterKm2
          // ignore: cast_nullable_to_non_nullable
          : afterKm2 as num?,
      deltaKm2: deltaKm2 == const $CopyWithPlaceholder()
          ? _value.deltaKm2
          // ignore: cast_nullable_to_non_nullable
          : deltaKm2 as num?,
    );
  }
}

extension $SituationChangesAreaCopyWith on SituationChangesArea {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationChangesArea.copyWith(...)` or `instanceOfSituationChangesArea.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationChangesAreaCWProxy get copyWith =>
      _$SituationChangesAreaCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationChangesArea _$SituationChangesAreaFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SituationChangesArea', json, ($checkedConvert) {
  final val = SituationChangesArea(
    beforeKm2: $checkedConvert('beforeKm2', (v) => v as num?),
    afterKm2: $checkedConvert('afterKm2', (v) => v as num?),
    deltaKm2: $checkedConvert('deltaKm2', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$SituationChangesAreaToJson(
  SituationChangesArea instance,
) => <String, dynamic>{
  'beforeKm2': ?instance.beforeKm2,
  'afterKm2': ?instance.afterKm2,
  'deltaKm2': ?instance.deltaKm2,
};
