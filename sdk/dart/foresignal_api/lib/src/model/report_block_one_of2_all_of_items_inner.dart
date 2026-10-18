//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/object_envelope.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_block_one_of2_all_of_items_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportBlockOneOf2AllOfItemsInner {
  /// Returns a new [ReportBlockOneOf2AllOfItemsInner] instance.
  ReportBlockOneOf2AllOfItemsInner({

    required  this.ref,

     this.envelope,
  });

      /// Pinned reference (`@{seq}` for records) or snapshot id
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    
    name: r'envelope',
    required: false,
    includeIfNull: false,
  )


  final ObjectEnvelope? envelope;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportBlockOneOf2AllOfItemsInner &&
      other.ref == ref &&
      other.envelope == envelope;

    @override
    int get hashCode =>
        ref.hashCode +
        envelope.hashCode;

  factory ReportBlockOneOf2AllOfItemsInner.fromJson(Map<String, dynamic> json) => _$ReportBlockOneOf2AllOfItemsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ReportBlockOneOf2AllOfItemsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

