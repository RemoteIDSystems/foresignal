// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'index_weights_proposed_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IndexWeightsProposedEventCWProxy {
  IndexWeightsProposedEvent indexId(String indexId);

  IndexWeightsProposedEvent fitId(String fitId);

  IndexWeightsProposedEvent weightsVersion(int weightsVersion);

  IndexWeightsProposedEvent proposedWeights(Map<String, num> proposedWeights);

  IndexWeightsProposedEvent skill(RegionalIndexDefSkill? skill);

  IndexWeightsProposedEvent autoApplied(bool? autoApplied);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndexWeightsProposedEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndexWeightsProposedEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  IndexWeightsProposedEvent call({
    String indexId,
    String fitId,
    int weightsVersion,
    Map<String, num> proposedWeights,
    RegionalIndexDefSkill? skill,
    bool? autoApplied,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIndexWeightsProposedEvent.copyWith(...)` or call `instanceOfIndexWeightsProposedEvent.copyWith.fieldName(value)` for a single field.
class _$IndexWeightsProposedEventCWProxyImpl
    implements _$IndexWeightsProposedEventCWProxy {
  const _$IndexWeightsProposedEventCWProxyImpl(this._value);

  final IndexWeightsProposedEvent _value;

  @override
  IndexWeightsProposedEvent indexId(String indexId) => call(indexId: indexId);

  @override
  IndexWeightsProposedEvent fitId(String fitId) => call(fitId: fitId);

  @override
  IndexWeightsProposedEvent weightsVersion(int weightsVersion) =>
      call(weightsVersion: weightsVersion);

  @override
  IndexWeightsProposedEvent proposedWeights(Map<String, num> proposedWeights) =>
      call(proposedWeights: proposedWeights);

  @override
  IndexWeightsProposedEvent skill(RegionalIndexDefSkill? skill) =>
      call(skill: skill);

  @override
  IndexWeightsProposedEvent autoApplied(bool? autoApplied) =>
      call(autoApplied: autoApplied);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndexWeightsProposedEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndexWeightsProposedEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IndexWeightsProposedEvent call({
    Object? indexId = const $CopyWithPlaceholder(),
    Object? fitId = const $CopyWithPlaceholder(),
    Object? weightsVersion = const $CopyWithPlaceholder(),
    Object? proposedWeights = const $CopyWithPlaceholder(),
    Object? skill = const $CopyWithPlaceholder(),
    Object? autoApplied = const $CopyWithPlaceholder(),
  }) {
    return IndexWeightsProposedEvent(
      indexId: indexId == const $CopyWithPlaceholder() || indexId == null
          ? _value.indexId
          // ignore: cast_nullable_to_non_nullable
          : indexId as String,
      fitId: fitId == const $CopyWithPlaceholder() || fitId == null
          ? _value.fitId
          // ignore: cast_nullable_to_non_nullable
          : fitId as String,
      weightsVersion:
          weightsVersion == const $CopyWithPlaceholder() ||
              weightsVersion == null
          ? _value.weightsVersion
          // ignore: cast_nullable_to_non_nullable
          : weightsVersion as int,
      proposedWeights:
          proposedWeights == const $CopyWithPlaceholder() ||
              proposedWeights == null
          ? _value.proposedWeights
          // ignore: cast_nullable_to_non_nullable
          : proposedWeights as Map<String, num>,
      skill: skill == const $CopyWithPlaceholder()
          ? _value.skill
          // ignore: cast_nullable_to_non_nullable
          : skill as RegionalIndexDefSkill?,
      autoApplied: autoApplied == const $CopyWithPlaceholder()
          ? _value.autoApplied
          // ignore: cast_nullable_to_non_nullable
          : autoApplied as bool?,
    );
  }
}

extension $IndexWeightsProposedEventCopyWith on IndexWeightsProposedEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIndexWeightsProposedEvent.copyWith(...)` or `instanceOfIndexWeightsProposedEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IndexWeightsProposedEventCWProxy get copyWith =>
      _$IndexWeightsProposedEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IndexWeightsProposedEvent _$IndexWeightsProposedEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IndexWeightsProposedEvent', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'indexId',
      'fitId',
      'weightsVersion',
      'proposedWeights',
    ],
  );
  final val = IndexWeightsProposedEvent(
    indexId: $checkedConvert('indexId', (v) => v as String),
    fitId: $checkedConvert('fitId', (v) => v as String),
    weightsVersion: $checkedConvert(
      'weightsVersion',
      (v) => (v as num).toInt(),
    ),
    proposedWeights: $checkedConvert(
      'proposedWeights',
      (v) => Map<String, num>.from(v as Map),
    ),
    skill: $checkedConvert(
      'skill',
      (v) => v == null
          ? null
          : RegionalIndexDefSkill.fromJson(v as Map<String, dynamic>),
    ),
    autoApplied: $checkedConvert('autoApplied', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$IndexWeightsProposedEventToJson(
  IndexWeightsProposedEvent instance,
) => <String, dynamic>{
  'indexId': instance.indexId,
  'fitId': instance.fitId,
  'weightsVersion': instance.weightsVersion,
  'proposedWeights': instance.proposedWeights,
  'skill': ?instance.skill?.toJson(),
  'autoApplied': ?instance.autoApplied,
};
