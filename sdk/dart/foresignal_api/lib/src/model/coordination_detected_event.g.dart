// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'coordination_detected_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CoordinationDetectedEventCWProxy {
  CoordinationDetectedEvent event(CoordinationDetectedEventEventEnum event);

  CoordinationDetectedEvent findingId(String findingId);

  CoordinationDetectedEvent narrativeId(String narrativeId);

  CoordinationDetectedEvent significantIndicators(
    List<CoordinationDetectedEventSignificantIndicatorsEnum>
    significantIndicators,
  );

  CoordinationDetectedEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CoordinationDetectedEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CoordinationDetectedEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  CoordinationDetectedEvent call({
    CoordinationDetectedEventEventEnum event,
    String findingId,
    String narrativeId,
    List<CoordinationDetectedEventSignificantIndicatorsEnum>
    significantIndicators,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCoordinationDetectedEvent.copyWith(...)` or call `instanceOfCoordinationDetectedEvent.copyWith.fieldName(value)` for a single field.
class _$CoordinationDetectedEventCWProxyImpl
    implements _$CoordinationDetectedEventCWProxy {
  const _$CoordinationDetectedEventCWProxyImpl(this._value);

  final CoordinationDetectedEvent _value;

  @override
  CoordinationDetectedEvent event(CoordinationDetectedEventEventEnum event) =>
      call(event: event);

  @override
  CoordinationDetectedEvent findingId(String findingId) =>
      call(findingId: findingId);

  @override
  CoordinationDetectedEvent narrativeId(String narrativeId) =>
      call(narrativeId: narrativeId);

  @override
  CoordinationDetectedEvent significantIndicators(
    List<CoordinationDetectedEventSignificantIndicatorsEnum>
    significantIndicators,
  ) => call(significantIndicators: significantIndicators);

  @override
  CoordinationDetectedEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CoordinationDetectedEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CoordinationDetectedEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CoordinationDetectedEvent call({
    Object? event = const $CopyWithPlaceholder(),
    Object? findingId = const $CopyWithPlaceholder(),
    Object? narrativeId = const $CopyWithPlaceholder(),
    Object? significantIndicators = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return CoordinationDetectedEvent(
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as CoordinationDetectedEventEventEnum,
      findingId: findingId == const $CopyWithPlaceholder() || findingId == null
          ? _value.findingId
          // ignore: cast_nullable_to_non_nullable
          : findingId as String,
      narrativeId:
          narrativeId == const $CopyWithPlaceholder() || narrativeId == null
          ? _value.narrativeId
          // ignore: cast_nullable_to_non_nullable
          : narrativeId as String,
      significantIndicators:
          significantIndicators == const $CopyWithPlaceholder() ||
              significantIndicators == null
          ? _value.significantIndicators
          // ignore: cast_nullable_to_non_nullable
          : significantIndicators
                as List<CoordinationDetectedEventSignificantIndicatorsEnum>,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $CoordinationDetectedEventCopyWith on CoordinationDetectedEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCoordinationDetectedEvent.copyWith(...)` or `instanceOfCoordinationDetectedEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CoordinationDetectedEventCWProxy get copyWith =>
      _$CoordinationDetectedEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CoordinationDetectedEvent _$CoordinationDetectedEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CoordinationDetectedEvent', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'event',
      'findingId',
      'narrativeId',
      'significantIndicators',
      'at',
    ],
  );
  final val = CoordinationDetectedEvent(
    event: $checkedConvert(
      'event',
      (v) => $enumDecode(_$CoordinationDetectedEventEventEnumEnumMap, v),
    ),
    findingId: $checkedConvert('findingId', (v) => v as String),
    narrativeId: $checkedConvert('narrativeId', (v) => v as String),
    significantIndicators: $checkedConvert(
      'significantIndicators',
      (v) => (v as List<dynamic>)
          .map(
            (e) => $enumDecode(
              _$CoordinationDetectedEventSignificantIndicatorsEnumEnumMap,
              e,
            ),
          )
          .toList(),
    ),
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$CoordinationDetectedEventToJson(
  CoordinationDetectedEvent instance,
) => <String, dynamic>{
  'event': _$CoordinationDetectedEventEventEnumEnumMap[instance.event]!,
  'findingId': instance.findingId,
  'narrativeId': instance.narrativeId,
  'significantIndicators': instance.significantIndicators
      .map(
        (e) => _$CoordinationDetectedEventSignificantIndicatorsEnumEnumMap[e]!,
      )
      .toList(),
  'at': instance.at.toIso8601String(),
};

const _$CoordinationDetectedEventEventEnumEnumMap = {
  CoordinationDetectedEventEventEnum.coordinationPeriodDetected:
      'coordination.detected',
};

const _$CoordinationDetectedEventSignificantIndicatorsEnumEnumMap = {
  CoordinationDetectedEventSignificantIndicatorsEnum.synchrony: 'synchrony',
  CoordinationDetectedEventSignificantIndicatorsEnum.textDuplication:
      'textDuplication',
  CoordinationDetectedEventSignificantIndicatorsEnum.accountAgeSkew:
      'accountAgeSkew',
  CoordinationDetectedEventSignificantIndicatorsEnum.coRepostBursts:
      'coRepostBursts',
};
