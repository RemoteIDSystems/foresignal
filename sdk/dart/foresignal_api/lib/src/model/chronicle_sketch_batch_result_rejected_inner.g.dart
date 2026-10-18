// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_sketch_batch_result_rejected_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleSketchBatchResultRejectedInnerCWProxy {
  ChronicleSketchBatchResultRejectedInner sketchId(String sketchId);

  ChronicleSketchBatchResultRejectedInner reason(String reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSketchBatchResultRejectedInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSketchBatchResultRejectedInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleSketchBatchResultRejectedInner call({
    String sketchId,
    String reason,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleSketchBatchResultRejectedInner.copyWith(...)` or call `instanceOfChronicleSketchBatchResultRejectedInner.copyWith.fieldName(value)` for a single field.
class _$ChronicleSketchBatchResultRejectedInnerCWProxyImpl
    implements _$ChronicleSketchBatchResultRejectedInnerCWProxy {
  const _$ChronicleSketchBatchResultRejectedInnerCWProxyImpl(this._value);

  final ChronicleSketchBatchResultRejectedInner _value;

  @override
  ChronicleSketchBatchResultRejectedInner sketchId(String sketchId) =>
      call(sketchId: sketchId);

  @override
  ChronicleSketchBatchResultRejectedInner reason(String reason) =>
      call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSketchBatchResultRejectedInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSketchBatchResultRejectedInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleSketchBatchResultRejectedInner call({
    Object? sketchId = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return ChronicleSketchBatchResultRejectedInner(
      sketchId: sketchId == const $CopyWithPlaceholder() || sketchId == null
          ? _value.sketchId
          // ignore: cast_nullable_to_non_nullable
          : sketchId as String,
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String,
    );
  }
}

extension $ChronicleSketchBatchResultRejectedInnerCopyWith
    on ChronicleSketchBatchResultRejectedInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleSketchBatchResultRejectedInner.copyWith(...)` or `instanceOfChronicleSketchBatchResultRejectedInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleSketchBatchResultRejectedInnerCWProxy get copyWith =>
      _$ChronicleSketchBatchResultRejectedInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleSketchBatchResultRejectedInner
_$ChronicleSketchBatchResultRejectedInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ChronicleSketchBatchResultRejectedInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['sketchId', 'reason']);
      final val = ChronicleSketchBatchResultRejectedInner(
        sketchId: $checkedConvert('sketchId', (v) => v as String),
        reason: $checkedConvert('reason', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$ChronicleSketchBatchResultRejectedInnerToJson(
  ChronicleSketchBatchResultRejectedInner instance,
) => <String, dynamic>{
  'sketchId': instance.sketchId,
  'reason': instance.reason,
};
