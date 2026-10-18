// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_sketch_batch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleSketchBatchCWProxy {
  ChronicleSketchBatch instanceId(String instanceId);

  ChronicleSketchBatch networkId(String networkId);

  ChronicleSketchBatch sketches(List<ChronicleSketchPackage> sketches);

  ChronicleSketchBatch withdrawn(List<String>? withdrawn);

  ChronicleSketchBatch accessRequests(
    List<ChronicleSketchBatchAccessRequestsInner>? accessRequests,
  );

  ChronicleSketchBatch accessDecisions(
    List<ChronicleSketchBatchAccessDecisionsInner>? accessDecisions,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSketchBatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSketchBatch(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleSketchBatch call({
    String instanceId,
    String networkId,
    List<ChronicleSketchPackage> sketches,
    List<String>? withdrawn,
    List<ChronicleSketchBatchAccessRequestsInner>? accessRequests,
    List<ChronicleSketchBatchAccessDecisionsInner>? accessDecisions,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleSketchBatch.copyWith(...)` or call `instanceOfChronicleSketchBatch.copyWith.fieldName(value)` for a single field.
class _$ChronicleSketchBatchCWProxyImpl
    implements _$ChronicleSketchBatchCWProxy {
  const _$ChronicleSketchBatchCWProxyImpl(this._value);

  final ChronicleSketchBatch _value;

  @override
  ChronicleSketchBatch instanceId(String instanceId) =>
      call(instanceId: instanceId);

  @override
  ChronicleSketchBatch networkId(String networkId) =>
      call(networkId: networkId);

  @override
  ChronicleSketchBatch sketches(List<ChronicleSketchPackage> sketches) =>
      call(sketches: sketches);

  @override
  ChronicleSketchBatch withdrawn(List<String>? withdrawn) =>
      call(withdrawn: withdrawn);

  @override
  ChronicleSketchBatch accessRequests(
    List<ChronicleSketchBatchAccessRequestsInner>? accessRequests,
  ) => call(accessRequests: accessRequests);

  @override
  ChronicleSketchBatch accessDecisions(
    List<ChronicleSketchBatchAccessDecisionsInner>? accessDecisions,
  ) => call(accessDecisions: accessDecisions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSketchBatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSketchBatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleSketchBatch call({
    Object? instanceId = const $CopyWithPlaceholder(),
    Object? networkId = const $CopyWithPlaceholder(),
    Object? sketches = const $CopyWithPlaceholder(),
    Object? withdrawn = const $CopyWithPlaceholder(),
    Object? accessRequests = const $CopyWithPlaceholder(),
    Object? accessDecisions = const $CopyWithPlaceholder(),
  }) {
    return ChronicleSketchBatch(
      instanceId:
          instanceId == const $CopyWithPlaceholder() || instanceId == null
          ? _value.instanceId
          // ignore: cast_nullable_to_non_nullable
          : instanceId as String,
      networkId: networkId == const $CopyWithPlaceholder() || networkId == null
          ? _value.networkId
          // ignore: cast_nullable_to_non_nullable
          : networkId as String,
      sketches: sketches == const $CopyWithPlaceholder() || sketches == null
          ? _value.sketches
          // ignore: cast_nullable_to_non_nullable
          : sketches as List<ChronicleSketchPackage>,
      withdrawn: withdrawn == const $CopyWithPlaceholder()
          ? _value.withdrawn
          // ignore: cast_nullable_to_non_nullable
          : withdrawn as List<String>?,
      accessRequests: accessRequests == const $CopyWithPlaceholder()
          ? _value.accessRequests
          // ignore: cast_nullable_to_non_nullable
          : accessRequests as List<ChronicleSketchBatchAccessRequestsInner>?,
      accessDecisions: accessDecisions == const $CopyWithPlaceholder()
          ? _value.accessDecisions
          // ignore: cast_nullable_to_non_nullable
          : accessDecisions as List<ChronicleSketchBatchAccessDecisionsInner>?,
    );
  }
}

extension $ChronicleSketchBatchCopyWith on ChronicleSketchBatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleSketchBatch.copyWith(...)` or `instanceOfChronicleSketchBatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleSketchBatchCWProxy get copyWith =>
      _$ChronicleSketchBatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleSketchBatch _$ChronicleSketchBatchFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleSketchBatch', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['instanceId', 'networkId', 'sketches']);
  final val = ChronicleSketchBatch(
    instanceId: $checkedConvert('instanceId', (v) => v as String),
    networkId: $checkedConvert('networkId', (v) => v as String),
    sketches: $checkedConvert(
      'sketches',
      (v) => (v as List<dynamic>)
          .map(
            (e) => ChronicleSketchPackage.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    withdrawn: $checkedConvert(
      'withdrawn',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    accessRequests: $checkedConvert(
      'accessRequests',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => ChronicleSketchBatchAccessRequestsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    accessDecisions: $checkedConvert(
      'accessDecisions',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => ChronicleSketchBatchAccessDecisionsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ChronicleSketchBatchToJson(
  ChronicleSketchBatch instance,
) => <String, dynamic>{
  'instanceId': instance.instanceId,
  'networkId': instance.networkId,
  'sketches': instance.sketches.map((e) => e.toJson()).toList(),
  'withdrawn': ?instance.withdrawn,
  'accessRequests': ?instance.accessRequests?.map((e) => e.toJson()).toList(),
  'accessDecisions': ?instance.accessDecisions?.map((e) => e.toJson()).toList(),
};
