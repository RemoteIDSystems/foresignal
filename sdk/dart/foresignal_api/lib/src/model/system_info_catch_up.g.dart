// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system_info_catch_up.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SystemInfoCatchUpCWProxy {
  SystemInfoCatchUp triggerLagSeq(int? triggerLagSeq);

  SystemInfoCatchUp ingestQueueDepth(int? ingestQueueDepth);

  SystemInfoCatchUp catchingUp(bool? catchingUp);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoCatchUp(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoCatchUp(...).copyWith(id: 12, name: "My name")
  /// ```
  SystemInfoCatchUp call({
    int? triggerLagSeq,
    int? ingestQueueDepth,
    bool? catchingUp,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSystemInfoCatchUp.copyWith(...)` or call `instanceOfSystemInfoCatchUp.copyWith.fieldName(value)` for a single field.
class _$SystemInfoCatchUpCWProxyImpl implements _$SystemInfoCatchUpCWProxy {
  const _$SystemInfoCatchUpCWProxyImpl(this._value);

  final SystemInfoCatchUp _value;

  @override
  SystemInfoCatchUp triggerLagSeq(int? triggerLagSeq) =>
      call(triggerLagSeq: triggerLagSeq);

  @override
  SystemInfoCatchUp ingestQueueDepth(int? ingestQueueDepth) =>
      call(ingestQueueDepth: ingestQueueDepth);

  @override
  SystemInfoCatchUp catchingUp(bool? catchingUp) =>
      call(catchingUp: catchingUp);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoCatchUp(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoCatchUp(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SystemInfoCatchUp call({
    Object? triggerLagSeq = const $CopyWithPlaceholder(),
    Object? ingestQueueDepth = const $CopyWithPlaceholder(),
    Object? catchingUp = const $CopyWithPlaceholder(),
  }) {
    return SystemInfoCatchUp(
      triggerLagSeq: triggerLagSeq == const $CopyWithPlaceholder()
          ? _value.triggerLagSeq
          // ignore: cast_nullable_to_non_nullable
          : triggerLagSeq as int?,
      ingestQueueDepth: ingestQueueDepth == const $CopyWithPlaceholder()
          ? _value.ingestQueueDepth
          // ignore: cast_nullable_to_non_nullable
          : ingestQueueDepth as int?,
      catchingUp: catchingUp == const $CopyWithPlaceholder()
          ? _value.catchingUp
          // ignore: cast_nullable_to_non_nullable
          : catchingUp as bool?,
    );
  }
}

extension $SystemInfoCatchUpCopyWith on SystemInfoCatchUp {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSystemInfoCatchUp.copyWith(...)` or `instanceOfSystemInfoCatchUp.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SystemInfoCatchUpCWProxy get copyWith =>
      _$SystemInfoCatchUpCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SystemInfoCatchUp _$SystemInfoCatchUpFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SystemInfoCatchUp', json, ($checkedConvert) {
      final val = SystemInfoCatchUp(
        triggerLagSeq: $checkedConvert(
          'triggerLagSeq',
          (v) => (v as num?)?.toInt(),
        ),
        ingestQueueDepth: $checkedConvert(
          'ingestQueueDepth',
          (v) => (v as num?)?.toInt(),
        ),
        catchingUp: $checkedConvert('catchingUp', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$SystemInfoCatchUpToJson(SystemInfoCatchUp instance) =>
    <String, dynamic>{
      'triggerLagSeq': ?instance.triggerLagSeq,
      'ingestQueueDepth': ?instance.ingestQueueDepth,
      'catchingUp': ?instance.catchingUp,
    };
