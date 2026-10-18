// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cycle_packet_forecasts.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CyclePacketForecastsCWProxy {
  CyclePacketForecasts opened(List<CyclePacketItem>? opened);

  CyclePacketForecasts due(List<CyclePacketItem>? due);

  CyclePacketForecasts resolved(List<CyclePacketItem>? resolved);

  CyclePacketForecasts scorecardChanges(
    List<CyclePacketItem>? scorecardChanges,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CyclePacketForecasts(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CyclePacketForecasts(...).copyWith(id: 12, name: "My name")
  /// ```
  CyclePacketForecasts call({
    List<CyclePacketItem>? opened,
    List<CyclePacketItem>? due,
    List<CyclePacketItem>? resolved,
    List<CyclePacketItem>? scorecardChanges,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCyclePacketForecasts.copyWith(...)` or call `instanceOfCyclePacketForecasts.copyWith.fieldName(value)` for a single field.
class _$CyclePacketForecastsCWProxyImpl
    implements _$CyclePacketForecastsCWProxy {
  const _$CyclePacketForecastsCWProxyImpl(this._value);

  final CyclePacketForecasts _value;

  @override
  CyclePacketForecasts opened(List<CyclePacketItem>? opened) =>
      call(opened: opened);

  @override
  CyclePacketForecasts due(List<CyclePacketItem>? due) => call(due: due);

  @override
  CyclePacketForecasts resolved(List<CyclePacketItem>? resolved) =>
      call(resolved: resolved);

  @override
  CyclePacketForecasts scorecardChanges(
    List<CyclePacketItem>? scorecardChanges,
  ) => call(scorecardChanges: scorecardChanges);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CyclePacketForecasts(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CyclePacketForecasts(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CyclePacketForecasts call({
    Object? opened = const $CopyWithPlaceholder(),
    Object? due = const $CopyWithPlaceholder(),
    Object? resolved = const $CopyWithPlaceholder(),
    Object? scorecardChanges = const $CopyWithPlaceholder(),
  }) {
    return CyclePacketForecasts(
      opened: opened == const $CopyWithPlaceholder()
          ? _value.opened
          // ignore: cast_nullable_to_non_nullable
          : opened as List<CyclePacketItem>?,
      due: due == const $CopyWithPlaceholder()
          ? _value.due
          // ignore: cast_nullable_to_non_nullable
          : due as List<CyclePacketItem>?,
      resolved: resolved == const $CopyWithPlaceholder()
          ? _value.resolved
          // ignore: cast_nullable_to_non_nullable
          : resolved as List<CyclePacketItem>?,
      scorecardChanges: scorecardChanges == const $CopyWithPlaceholder()
          ? _value.scorecardChanges
          // ignore: cast_nullable_to_non_nullable
          : scorecardChanges as List<CyclePacketItem>?,
    );
  }
}

extension $CyclePacketForecastsCopyWith on CyclePacketForecasts {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCyclePacketForecasts.copyWith(...)` or `instanceOfCyclePacketForecasts.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CyclePacketForecastsCWProxy get copyWith =>
      _$CyclePacketForecastsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CyclePacketForecasts _$CyclePacketForecastsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CyclePacketForecasts', json, ($checkedConvert) {
  final val = CyclePacketForecasts(
    opened: $checkedConvert(
      'opened',
      (v) => (v as List<dynamic>?)
          ?.map((e) => CyclePacketItem.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    due: $checkedConvert(
      'due',
      (v) => (v as List<dynamic>?)
          ?.map((e) => CyclePacketItem.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    resolved: $checkedConvert(
      'resolved',
      (v) => (v as List<dynamic>?)
          ?.map((e) => CyclePacketItem.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    scorecardChanges: $checkedConvert(
      'scorecardChanges',
      (v) => (v as List<dynamic>?)
          ?.map((e) => CyclePacketItem.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$CyclePacketForecastsToJson(
  CyclePacketForecasts instance,
) => <String, dynamic>{
  'opened': ?instance.opened?.map((e) => e.toJson()).toList(),
  'due': ?instance.due?.map((e) => e.toJson()).toList(),
  'resolved': ?instance.resolved?.map((e) => e.toJson()).toList(),
  'scorecardChanges': ?instance.scorecardChanges
      ?.map((e) => e.toJson())
      .toList(),
};
