// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'claim_extracted_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ClaimExtractedEventCWProxy {
  ClaimExtractedEvent event(ClaimExtractedEventEventEnum event);

  ClaimExtractedEvent claim(Claim claim);

  ClaimExtractedEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ClaimExtractedEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ClaimExtractedEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  ClaimExtractedEvent call({
    ClaimExtractedEventEventEnum event,
    Claim claim,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfClaimExtractedEvent.copyWith(...)` or call `instanceOfClaimExtractedEvent.copyWith.fieldName(value)` for a single field.
class _$ClaimExtractedEventCWProxyImpl implements _$ClaimExtractedEventCWProxy {
  const _$ClaimExtractedEventCWProxyImpl(this._value);

  final ClaimExtractedEvent _value;

  @override
  ClaimExtractedEvent event(ClaimExtractedEventEventEnum event) =>
      call(event: event);

  @override
  ClaimExtractedEvent claim(Claim claim) => call(claim: claim);

  @override
  ClaimExtractedEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ClaimExtractedEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ClaimExtractedEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ClaimExtractedEvent call({
    Object? event = const $CopyWithPlaceholder(),
    Object? claim = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return ClaimExtractedEvent(
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as ClaimExtractedEventEventEnum,
      claim: claim == const $CopyWithPlaceholder() || claim == null
          ? _value.claim
          // ignore: cast_nullable_to_non_nullable
          : claim as Claim,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $ClaimExtractedEventCopyWith on ClaimExtractedEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfClaimExtractedEvent.copyWith(...)` or `instanceOfClaimExtractedEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ClaimExtractedEventCWProxy get copyWith =>
      _$ClaimExtractedEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ClaimExtractedEvent _$ClaimExtractedEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ClaimExtractedEvent', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['event', 'claim', 'at']);
      final val = ClaimExtractedEvent(
        event: $checkedConvert(
          'event',
          (v) => $enumDecode(_$ClaimExtractedEventEventEnumEnumMap, v),
        ),
        claim: $checkedConvert(
          'claim',
          (v) => Claim.fromJson(v as Map<String, dynamic>),
        ),
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
      );
      return val;
    });

Map<String, dynamic> _$ClaimExtractedEventToJson(
  ClaimExtractedEvent instance,
) => <String, dynamic>{
  'event': _$ClaimExtractedEventEventEnumEnumMap[instance.event]!,
  'claim': instance.claim.toJson(),
  'at': instance.at.toIso8601String(),
};

const _$ClaimExtractedEventEventEnumEnumMap = {
  ClaimExtractedEventEventEnum.claimPeriodExtracted: 'claim.extracted',
};
