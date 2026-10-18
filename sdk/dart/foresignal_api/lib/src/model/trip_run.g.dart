// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'trip_run.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TripRunCWProxy {
  TripRun id(String id);

  TripRun tripId(String tripId);

  TripRun occurrence(DateTime occurrence);

  TripRun status(TripRunStatusEnum status);

  TripRun clear(bool clear);

  TripRun hazards(List<TripHazard> hazards);

  TripRun briefing(String? briefing);

  TripRun deliveredAt(DateTime? deliveredAt);

  TripRun createdAt(DateTime createdAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TripRun(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TripRun(...).copyWith(id: 12, name: "My name")
  /// ```
  TripRun call({
    String id,
    String tripId,
    DateTime occurrence,
    TripRunStatusEnum status,
    bool clear,
    List<TripHazard> hazards,
    String? briefing,
    DateTime? deliveredAt,
    DateTime createdAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTripRun.copyWith(...)` or call `instanceOfTripRun.copyWith.fieldName(value)` for a single field.
class _$TripRunCWProxyImpl implements _$TripRunCWProxy {
  const _$TripRunCWProxyImpl(this._value);

  final TripRun _value;

  @override
  TripRun id(String id) => call(id: id);

  @override
  TripRun tripId(String tripId) => call(tripId: tripId);

  @override
  TripRun occurrence(DateTime occurrence) => call(occurrence: occurrence);

  @override
  TripRun status(TripRunStatusEnum status) => call(status: status);

  @override
  TripRun clear(bool clear) => call(clear: clear);

  @override
  TripRun hazards(List<TripHazard> hazards) => call(hazards: hazards);

  @override
  TripRun briefing(String? briefing) => call(briefing: briefing);

  @override
  TripRun deliveredAt(DateTime? deliveredAt) => call(deliveredAt: deliveredAt);

  @override
  TripRun createdAt(DateTime createdAt) => call(createdAt: createdAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TripRun(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TripRun(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TripRun call({
    Object? id = const $CopyWithPlaceholder(),
    Object? tripId = const $CopyWithPlaceholder(),
    Object? occurrence = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? clear = const $CopyWithPlaceholder(),
    Object? hazards = const $CopyWithPlaceholder(),
    Object? briefing = const $CopyWithPlaceholder(),
    Object? deliveredAt = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
  }) {
    return TripRun(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      tripId: tripId == const $CopyWithPlaceholder() || tripId == null
          ? _value.tripId
          // ignore: cast_nullable_to_non_nullable
          : tripId as String,
      occurrence:
          occurrence == const $CopyWithPlaceholder() || occurrence == null
          ? _value.occurrence
          // ignore: cast_nullable_to_non_nullable
          : occurrence as DateTime,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as TripRunStatusEnum,
      clear: clear == const $CopyWithPlaceholder() || clear == null
          ? _value.clear
          // ignore: cast_nullable_to_non_nullable
          : clear as bool,
      hazards: hazards == const $CopyWithPlaceholder() || hazards == null
          ? _value.hazards
          // ignore: cast_nullable_to_non_nullable
          : hazards as List<TripHazard>,
      briefing: briefing == const $CopyWithPlaceholder()
          ? _value.briefing
          // ignore: cast_nullable_to_non_nullable
          : briefing as String?,
      deliveredAt: deliveredAt == const $CopyWithPlaceholder()
          ? _value.deliveredAt
          // ignore: cast_nullable_to_non_nullable
          : deliveredAt as DateTime?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
    );
  }
}

extension $TripRunCopyWith on TripRun {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTripRun.copyWith(...)` or `instanceOfTripRun.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TripRunCWProxy get copyWith => _$TripRunCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TripRun _$TripRunFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TripRun', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'id',
          'tripId',
          'occurrence',
          'status',
          'clear',
          'hazards',
          'createdAt',
        ],
      );
      final val = TripRun(
        id: $checkedConvert('id', (v) => v as String),
        tripId: $checkedConvert('tripId', (v) => v as String),
        occurrence: $checkedConvert(
          'occurrence',
          (v) => DateTime.parse(v as String),
        ),
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$TripRunStatusEnumEnumMap, v),
        ),
        clear: $checkedConvert('clear', (v) => v as bool),
        hazards: $checkedConvert(
          'hazards',
          (v) => (v as List<dynamic>)
              .map((e) => TripHazard.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        briefing: $checkedConvert('briefing', (v) => v as String?),
        deliveredAt: $checkedConvert(
          'deliveredAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        createdAt: $checkedConvert(
          'createdAt',
          (v) => DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$TripRunToJson(TripRun instance) => <String, dynamic>{
  'id': instance.id,
  'tripId': instance.tripId,
  'occurrence': instance.occurrence.toIso8601String(),
  'status': _$TripRunStatusEnumEnumMap[instance.status]!,
  'clear': instance.clear,
  'hazards': instance.hazards.map((e) => e.toJson()).toList(),
  'briefing': ?instance.briefing,
  'deliveredAt': ?instance.deliveredAt?.toIso8601String(),
  'createdAt': instance.createdAt.toIso8601String(),
};

const _$TripRunStatusEnumEnumMap = {
  TripRunStatusEnum.delivered: 'delivered',
  TripRunStatusEnum.skipped: 'skipped',
  TripRunStatusEnum.dryRun: 'dry-run',
  TripRunStatusEnum.failed: 'failed',
};
