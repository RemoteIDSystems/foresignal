// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_sketch_batch_access_decisions_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleSketchBatchAccessDecisionsInnerCWProxy {
  ChronicleSketchBatchAccessDecisionsInner requestId(String requestId);

  ChronicleSketchBatchAccessDecisionsInner status(
    ChronicleSketchBatchAccessDecisionsInnerStatusEnum status,
  );

  ChronicleSketchBatchAccessDecisionsInner deliveredAs(
    ChronicleSketchBatchAccessDecisionsInnerDeliveredAsEnum? deliveredAs,
  );

  ChronicleSketchBatchAccessDecisionsInner packageId(String? packageId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSketchBatchAccessDecisionsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSketchBatchAccessDecisionsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleSketchBatchAccessDecisionsInner call({
    String requestId,
    ChronicleSketchBatchAccessDecisionsInnerStatusEnum status,
    ChronicleSketchBatchAccessDecisionsInnerDeliveredAsEnum? deliveredAs,
    String? packageId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleSketchBatchAccessDecisionsInner.copyWith(...)` or call `instanceOfChronicleSketchBatchAccessDecisionsInner.copyWith.fieldName(value)` for a single field.
class _$ChronicleSketchBatchAccessDecisionsInnerCWProxyImpl
    implements _$ChronicleSketchBatchAccessDecisionsInnerCWProxy {
  const _$ChronicleSketchBatchAccessDecisionsInnerCWProxyImpl(this._value);

  final ChronicleSketchBatchAccessDecisionsInner _value;

  @override
  ChronicleSketchBatchAccessDecisionsInner requestId(String requestId) =>
      call(requestId: requestId);

  @override
  ChronicleSketchBatchAccessDecisionsInner status(
    ChronicleSketchBatchAccessDecisionsInnerStatusEnum status,
  ) => call(status: status);

  @override
  ChronicleSketchBatchAccessDecisionsInner deliveredAs(
    ChronicleSketchBatchAccessDecisionsInnerDeliveredAsEnum? deliveredAs,
  ) => call(deliveredAs: deliveredAs);

  @override
  ChronicleSketchBatchAccessDecisionsInner packageId(String? packageId) =>
      call(packageId: packageId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSketchBatchAccessDecisionsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSketchBatchAccessDecisionsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleSketchBatchAccessDecisionsInner call({
    Object? requestId = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? deliveredAs = const $CopyWithPlaceholder(),
    Object? packageId = const $CopyWithPlaceholder(),
  }) {
    return ChronicleSketchBatchAccessDecisionsInner(
      requestId: requestId == const $CopyWithPlaceholder() || requestId == null
          ? _value.requestId
          // ignore: cast_nullable_to_non_nullable
          : requestId as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as ChronicleSketchBatchAccessDecisionsInnerStatusEnum,
      deliveredAs: deliveredAs == const $CopyWithPlaceholder()
          ? _value.deliveredAs
          // ignore: cast_nullable_to_non_nullable
          : deliveredAs
                as ChronicleSketchBatchAccessDecisionsInnerDeliveredAsEnum?,
      packageId: packageId == const $CopyWithPlaceholder()
          ? _value.packageId
          // ignore: cast_nullable_to_non_nullable
          : packageId as String?,
    );
  }
}

extension $ChronicleSketchBatchAccessDecisionsInnerCopyWith
    on ChronicleSketchBatchAccessDecisionsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleSketchBatchAccessDecisionsInner.copyWith(...)` or `instanceOfChronicleSketchBatchAccessDecisionsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleSketchBatchAccessDecisionsInnerCWProxy get copyWith =>
      _$ChronicleSketchBatchAccessDecisionsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleSketchBatchAccessDecisionsInner
_$ChronicleSketchBatchAccessDecisionsInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ChronicleSketchBatchAccessDecisionsInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['requestId', 'status']);
      final val = ChronicleSketchBatchAccessDecisionsInner(
        requestId: $checkedConvert('requestId', (v) => v as String),
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(
            _$ChronicleSketchBatchAccessDecisionsInnerStatusEnumEnumMap,
            v,
          ),
        ),
        deliveredAs: $checkedConvert(
          'deliveredAs',
          (v) => $enumDecodeNullable(
            _$ChronicleSketchBatchAccessDecisionsInnerDeliveredAsEnumEnumMap,
            v,
          ),
        ),
        packageId: $checkedConvert('packageId', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ChronicleSketchBatchAccessDecisionsInnerToJson(
  ChronicleSketchBatchAccessDecisionsInner instance,
) => <String, dynamic>{
  'requestId': instance.requestId,
  'status':
      _$ChronicleSketchBatchAccessDecisionsInnerStatusEnumEnumMap[instance
          .status]!,
  'deliveredAs':
      ?_$ChronicleSketchBatchAccessDecisionsInnerDeliveredAsEnumEnumMap[instance
          .deliveredAs],
  'packageId': ?instance.packageId,
};

const _$ChronicleSketchBatchAccessDecisionsInnerStatusEnumEnumMap = {
  ChronicleSketchBatchAccessDecisionsInnerStatusEnum.granted: 'granted',
  ChronicleSketchBatchAccessDecisionsInnerStatusEnum.declined: 'declined',
};

const _$ChronicleSketchBatchAccessDecisionsInnerDeliveredAsEnumEnumMap = {
  ChronicleSketchBatchAccessDecisionsInnerDeliveredAsEnum.report: 'report',
  ChronicleSketchBatchAccessDecisionsInnerDeliveredAsEnum.chronicleExtract:
      'chronicle-extract',
};
