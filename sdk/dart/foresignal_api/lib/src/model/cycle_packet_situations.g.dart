// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cycle_packet_situations.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CyclePacketSituationsCWProxy {
  CyclePacketSituations new_(List<CyclePacketItem>? new_);

  CyclePacketSituations activated(List<CyclePacketItem>? activated);

  CyclePacketSituations escalated(List<CyclePacketItem>? escalated);

  CyclePacketSituations dormant(List<CyclePacketItem>? dormant);

  CyclePacketSituations closed(List<CyclePacketItem>? closed);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CyclePacketSituations(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CyclePacketSituations(...).copyWith(id: 12, name: "My name")
  /// ```
  CyclePacketSituations call({
    List<CyclePacketItem>? new_,
    List<CyclePacketItem>? activated,
    List<CyclePacketItem>? escalated,
    List<CyclePacketItem>? dormant,
    List<CyclePacketItem>? closed,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCyclePacketSituations.copyWith(...)` or call `instanceOfCyclePacketSituations.copyWith.fieldName(value)` for a single field.
class _$CyclePacketSituationsCWProxyImpl
    implements _$CyclePacketSituationsCWProxy {
  const _$CyclePacketSituationsCWProxyImpl(this._value);

  final CyclePacketSituations _value;

  @override
  CyclePacketSituations new_(List<CyclePacketItem>? new_) => call(new_: new_);

  @override
  CyclePacketSituations activated(List<CyclePacketItem>? activated) =>
      call(activated: activated);

  @override
  CyclePacketSituations escalated(List<CyclePacketItem>? escalated) =>
      call(escalated: escalated);

  @override
  CyclePacketSituations dormant(List<CyclePacketItem>? dormant) =>
      call(dormant: dormant);

  @override
  CyclePacketSituations closed(List<CyclePacketItem>? closed) =>
      call(closed: closed);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CyclePacketSituations(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CyclePacketSituations(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CyclePacketSituations call({
    Object? new_ = const $CopyWithPlaceholder(),
    Object? activated = const $CopyWithPlaceholder(),
    Object? escalated = const $CopyWithPlaceholder(),
    Object? dormant = const $CopyWithPlaceholder(),
    Object? closed = const $CopyWithPlaceholder(),
  }) {
    return CyclePacketSituations(
      new_: new_ == const $CopyWithPlaceholder()
          ? _value.new_
          // ignore: cast_nullable_to_non_nullable
          : new_ as List<CyclePacketItem>?,
      activated: activated == const $CopyWithPlaceholder()
          ? _value.activated
          // ignore: cast_nullable_to_non_nullable
          : activated as List<CyclePacketItem>?,
      escalated: escalated == const $CopyWithPlaceholder()
          ? _value.escalated
          // ignore: cast_nullable_to_non_nullable
          : escalated as List<CyclePacketItem>?,
      dormant: dormant == const $CopyWithPlaceholder()
          ? _value.dormant
          // ignore: cast_nullable_to_non_nullable
          : dormant as List<CyclePacketItem>?,
      closed: closed == const $CopyWithPlaceholder()
          ? _value.closed
          // ignore: cast_nullable_to_non_nullable
          : closed as List<CyclePacketItem>?,
    );
  }
}

extension $CyclePacketSituationsCopyWith on CyclePacketSituations {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCyclePacketSituations.copyWith(...)` or `instanceOfCyclePacketSituations.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CyclePacketSituationsCWProxy get copyWith =>
      _$CyclePacketSituationsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CyclePacketSituations _$CyclePacketSituationsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CyclePacketSituations', json, ($checkedConvert) {
  final val = CyclePacketSituations(
    new_: $checkedConvert(
      'new',
      (v) => (v as List<dynamic>?)
          ?.map((e) => CyclePacketItem.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    activated: $checkedConvert(
      'activated',
      (v) => (v as List<dynamic>?)
          ?.map((e) => CyclePacketItem.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    escalated: $checkedConvert(
      'escalated',
      (v) => (v as List<dynamic>?)
          ?.map((e) => CyclePacketItem.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    dormant: $checkedConvert(
      'dormant',
      (v) => (v as List<dynamic>?)
          ?.map((e) => CyclePacketItem.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    closed: $checkedConvert(
      'closed',
      (v) => (v as List<dynamic>?)
          ?.map((e) => CyclePacketItem.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
}, fieldKeyMap: const {'new_': 'new'});

Map<String, dynamic> _$CyclePacketSituationsToJson(
  CyclePacketSituations instance,
) => <String, dynamic>{
  'new': ?instance.new_?.map((e) => e.toJson()).toList(),
  'activated': ?instance.activated?.map((e) => e.toJson()).toList(),
  'escalated': ?instance.escalated?.map((e) => e.toJson()).toList(),
  'dormant': ?instance.dormant?.map((e) => e.toJson()).toList(),
  'closed': ?instance.closed?.map((e) => e.toJson()).toList(),
};
