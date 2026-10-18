// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_sketch_batch_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleSketchBatchResultCWProxy {
  ChronicleSketchBatchResult accepted(int accepted);

  ChronicleSketchBatchResult rejected(
    List<ChronicleSketchBatchResultRejectedInner> rejected,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSketchBatchResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSketchBatchResult(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleSketchBatchResult call({
    int accepted,
    List<ChronicleSketchBatchResultRejectedInner> rejected,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleSketchBatchResult.copyWith(...)` or call `instanceOfChronicleSketchBatchResult.copyWith.fieldName(value)` for a single field.
class _$ChronicleSketchBatchResultCWProxyImpl
    implements _$ChronicleSketchBatchResultCWProxy {
  const _$ChronicleSketchBatchResultCWProxyImpl(this._value);

  final ChronicleSketchBatchResult _value;

  @override
  ChronicleSketchBatchResult accepted(int accepted) => call(accepted: accepted);

  @override
  ChronicleSketchBatchResult rejected(
    List<ChronicleSketchBatchResultRejectedInner> rejected,
  ) => call(rejected: rejected);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSketchBatchResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSketchBatchResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleSketchBatchResult call({
    Object? accepted = const $CopyWithPlaceholder(),
    Object? rejected = const $CopyWithPlaceholder(),
  }) {
    return ChronicleSketchBatchResult(
      accepted: accepted == const $CopyWithPlaceholder() || accepted == null
          ? _value.accepted
          // ignore: cast_nullable_to_non_nullable
          : accepted as int,
      rejected: rejected == const $CopyWithPlaceholder() || rejected == null
          ? _value.rejected
          // ignore: cast_nullable_to_non_nullable
          : rejected as List<ChronicleSketchBatchResultRejectedInner>,
    );
  }
}

extension $ChronicleSketchBatchResultCopyWith on ChronicleSketchBatchResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleSketchBatchResult.copyWith(...)` or `instanceOfChronicleSketchBatchResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleSketchBatchResultCWProxy get copyWith =>
      _$ChronicleSketchBatchResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleSketchBatchResult _$ChronicleSketchBatchResultFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleSketchBatchResult', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['accepted', 'rejected']);
  final val = ChronicleSketchBatchResult(
    accepted: $checkedConvert('accepted', (v) => (v as num).toInt()),
    rejected: $checkedConvert(
      'rejected',
      (v) => (v as List<dynamic>)
          .map(
            (e) => ChronicleSketchBatchResultRejectedInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ChronicleSketchBatchResultToJson(
  ChronicleSketchBatchResult instance,
) => <String, dynamic>{
  'accepted': instance.accepted,
  'rejected': instance.rejected.map((e) => e.toJson()).toList(),
};
