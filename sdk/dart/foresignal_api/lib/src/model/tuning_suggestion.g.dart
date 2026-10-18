// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tuning_suggestion.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TuningSuggestionCWProxy {
  TuningSuggestion id(String id);

  TuningSuggestion kind(TuningSuggestionKindEnum kind);

  TuningSuggestion rationale(String rationale);

  TuningSuggestion patch_(Map<String, Object> patch_);

  TuningSuggestion expectedEffect(
    TuningSuggestionExpectedEffect? expectedEffect,
  );

  TuningSuggestion caveats(List<Caveat>? caveats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TuningSuggestion(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TuningSuggestion(...).copyWith(id: 12, name: "My name")
  /// ```
  TuningSuggestion call({
    String id,
    TuningSuggestionKindEnum kind,
    String rationale,
    Map<String, Object> patch_,
    TuningSuggestionExpectedEffect? expectedEffect,
    List<Caveat>? caveats,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTuningSuggestion.copyWith(...)` or call `instanceOfTuningSuggestion.copyWith.fieldName(value)` for a single field.
class _$TuningSuggestionCWProxyImpl implements _$TuningSuggestionCWProxy {
  const _$TuningSuggestionCWProxyImpl(this._value);

  final TuningSuggestion _value;

  @override
  TuningSuggestion id(String id) => call(id: id);

  @override
  TuningSuggestion kind(TuningSuggestionKindEnum kind) => call(kind: kind);

  @override
  TuningSuggestion rationale(String rationale) => call(rationale: rationale);

  @override
  TuningSuggestion patch_(Map<String, Object> patch_) => call(patch_: patch_);

  @override
  TuningSuggestion expectedEffect(
    TuningSuggestionExpectedEffect? expectedEffect,
  ) => call(expectedEffect: expectedEffect);

  @override
  TuningSuggestion caveats(List<Caveat>? caveats) => call(caveats: caveats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TuningSuggestion(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TuningSuggestion(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TuningSuggestion call({
    Object? id = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? rationale = const $CopyWithPlaceholder(),
    Object? patch_ = const $CopyWithPlaceholder(),
    Object? expectedEffect = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
  }) {
    return TuningSuggestion(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as TuningSuggestionKindEnum,
      rationale: rationale == const $CopyWithPlaceholder() || rationale == null
          ? _value.rationale
          // ignore: cast_nullable_to_non_nullable
          : rationale as String,
      patch_: patch_ == const $CopyWithPlaceholder() || patch_ == null
          ? _value.patch_
          // ignore: cast_nullable_to_non_nullable
          : patch_ as Map<String, Object>,
      expectedEffect: expectedEffect == const $CopyWithPlaceholder()
          ? _value.expectedEffect
          // ignore: cast_nullable_to_non_nullable
          : expectedEffect as TuningSuggestionExpectedEffect?,
      caveats: caveats == const $CopyWithPlaceholder()
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<Caveat>?,
    );
  }
}

extension $TuningSuggestionCopyWith on TuningSuggestion {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTuningSuggestion.copyWith(...)` or `instanceOfTuningSuggestion.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TuningSuggestionCWProxy get copyWith => _$TuningSuggestionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TuningSuggestion _$TuningSuggestionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TuningSuggestion', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id', 'kind', 'rationale', 'patch']);
  final val = TuningSuggestion(
    id: $checkedConvert('id', (v) => v as String),
    kind: $checkedConvert(
      'kind',
      (v) => $enumDecode(_$TuningSuggestionKindEnumEnumMap, v),
    ),
    rationale: $checkedConvert('rationale', (v) => v as String),
    patch_: $checkedConvert(
      'patch',
      (v) =>
          (v as Map<String, dynamic>).map((k, e) => MapEntry(k, e as Object)),
    ),
    expectedEffect: $checkedConvert(
      'expectedEffect',
      (v) => v == null
          ? null
          : TuningSuggestionExpectedEffect.fromJson(v as Map<String, dynamic>),
    ),
    caveats: $checkedConvert(
      'caveats',
      (v) => (v as List<dynamic>?)
          ?.map((e) => Caveat.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
}, fieldKeyMap: const {'patch_': 'patch'});

Map<String, dynamic> _$TuningSuggestionToJson(TuningSuggestion instance) =>
    <String, dynamic>{
      'id': instance.id,
      'kind': _$TuningSuggestionKindEnumEnumMap[instance.kind]!,
      'rationale': instance.rationale,
      'patch': instance.patch_,
      'expectedEffect': ?instance.expectedEffect?.toJson(),
      'caveats': ?instance.caveats?.map((e) => e.toJson()).toList(),
    };

const _$TuningSuggestionKindEnumEnumMap = {
  TuningSuggestionKindEnum.raiseThreshold: 'raise-threshold',
  TuningSuggestionKindEnum.lowerThreshold: 'lower-threshold',
  TuningSuggestionKindEnum.addCooldown: 'add-cooldown',
  TuningSuggestionKindEnum.switchToEnter: 'switch-to-enter',
  TuningSuggestionKindEnum.addEdgeMinInterval: 'add-edge-min-interval',
  TuningSuggestionKindEnum.shrinkGeofence: 'shrink-geofence',
  TuningSuggestionKindEnum.lowerAnomalySensitivity: 'lower-anomaly-sensitivity',
  TuningSuggestionKindEnum.ACTIVE_WINDOW: 'ACTIVE_WINDOW',
  TuningSuggestionKindEnum.SEVERITY_UP: 'SEVERITY_UP',
  TuningSuggestionKindEnum.SEVERITY_DOWN: 'SEVERITY_DOWN',
};
