//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'digest_all_of_next.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DigestAllOfNext {
  /// Returns a new [DigestAllOfNext] instance.
  DigestAllOfNext({

    required  this.sinceSeq,

     this.sinceAlertId,
  });

  @JsonKey(
    
    name: r'sinceSeq',
    required: true,
    includeIfNull: false,
  )


  final int sinceSeq;



  @JsonKey(
    
    name: r'sinceAlertId',
    required: false,
    includeIfNull: false,
  )


  final int? sinceAlertId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DigestAllOfNext &&
      other.sinceSeq == sinceSeq &&
      other.sinceAlertId == sinceAlertId;

    @override
    int get hashCode =>
        sinceSeq.hashCode +
        (sinceAlertId == null ? 0 : sinceAlertId.hashCode);

  factory DigestAllOfNext.fromJson(Map<String, dynamic> json) => _$DigestAllOfNextFromJson(json);

  Map<String, dynamic> toJson() => _$DigestAllOfNextToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

