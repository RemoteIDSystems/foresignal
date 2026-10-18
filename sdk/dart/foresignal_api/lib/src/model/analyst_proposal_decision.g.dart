// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analyst_proposal_decision.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalystProposalDecisionCWProxy {
  AnalystProposalDecision reason(String? reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystProposalDecision(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystProposalDecision(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalystProposalDecision call({String? reason});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalystProposalDecision.copyWith(...)` or call `instanceOfAnalystProposalDecision.copyWith.fieldName(value)` for a single field.
class _$AnalystProposalDecisionCWProxyImpl
    implements _$AnalystProposalDecisionCWProxy {
  const _$AnalystProposalDecisionCWProxyImpl(this._value);

  final AnalystProposalDecision _value;

  @override
  AnalystProposalDecision reason(String? reason) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystProposalDecision(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystProposalDecision(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalystProposalDecision call({
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return AnalystProposalDecision(
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
    );
  }
}

extension $AnalystProposalDecisionCopyWith on AnalystProposalDecision {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalystProposalDecision.copyWith(...)` or `instanceOfAnalystProposalDecision.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalystProposalDecisionCWProxy get copyWith =>
      _$AnalystProposalDecisionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalystProposalDecision _$AnalystProposalDecisionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnalystProposalDecision', json, ($checkedConvert) {
  final val = AnalystProposalDecision(
    reason: $checkedConvert('reason', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$AnalystProposalDecisionToJson(
  AnalystProposalDecision instance,
) => <String, dynamic>{'reason': ?instance.reason};
